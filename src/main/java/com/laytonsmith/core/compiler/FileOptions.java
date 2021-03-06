package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Prefs;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public final class FileOptions {
	/*
	 * These value names are used in the syntax highlighter, and should remain the name they are in code.
	 */
	@Option("Strict Mode On")
	private final Boolean strict;
	@Option("Suppressed Warnings")
	private final Set<SuppressWarning> suppressWarnings;
	@Option("File Name")
	private final String name;
	@Option("Author")
	private final String author;
	@Option("Creation Date")
	private final String created;
	@Option("File Description")
	private final String description;
	@Option("Required Extensions")
	private final Set<String> requiredExtensions;
	@Option("Compiler Options")
	private final Set<CompilerOption> compilerOptions;
	@Option("Copyright information")
	private final String copyright;
	@Option("Distribution License Information")
	private final String license;

	private final Map<String, String> rawOptions;
	//TODO: Make this non-public once this is all finished.

	public FileOptions(Map<String, String> parsedOptions) {
		rawOptions = new HashMap<>(parsedOptions);
		strict = parseBoolean(getDefault(parsedOptions, "strict", null));
		suppressWarnings = parseEnumSet(getDefault(parsedOptions, "suppresswarnings", ""), SuppressWarning.class);
		name = getDefault(parsedOptions, "name", "").trim();
		author = getDefault(parsedOptions, "author", "").trim();
		created = getDefault(parsedOptions, "created", "").trim();
		description = getDefault(parsedOptions, "description", "").trim();
		requiredExtensions = Collections.unmodifiableSet(parseSet(getDefault(parsedOptions, "requiredextensions", "")));
		compilerOptions = parseEnumSet(getDefault(parsedOptions, "compileroptions", ""), CompilerOption.class);
		copyright = getDefault(parsedOptions, "copyright", "").trim();
		license = getDefault(parsedOptions, "license", "").trim();
	}

	private String getDefault(Map<String, String> map, String key, String defaultIfNone) {
		if(map.containsKey(key)) {
			return map.get(key);
		} else {
			return defaultIfNone;
		}
	}

	private Boolean parseBoolean(String bool) {
		if(bool == null) {
			return null;
		}
		return !(bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("off"));
	}

	private List<String> parseList(String list) {
		List<String> l = new ArrayList<>();
		for(String part : list.split(",")) {
			if(!part.trim().isEmpty()) {
				l.add(part.trim().toLowerCase());
			}
		}
		return l;
	}

	private Set<String> parseSet(String list) {
		return new HashSet<>(parseList(list));
	}

	private <T extends Enum<T>> Set<T> parseEnumSet(String list, Class<T> type) {
		EnumSet<T> set = EnumSet.noneOf(type);
		List<String> sList = parseList(list);
		for(String s : sList) {
			for(T e : type.getEnumConstants()) {
				if(e.name().equals(s)) {
					set.add(e);
					break;
				}
			}
		}
		return set;
	}

	/**
	 * Returns whether or not this file is in strict mode. Unlike most options, this one depends on both the file
	 * options and the config value. In the config, if strict mode is turned on or off, this value only serves as the
	 * default. File options will override the global setting.
	 *
	 * @return
	 */
	public boolean isStrict() {
		if(strict != null) {
			return strict;
		} else {
			return Prefs.StrictMode();
		}
	}

	/**
	 * Returns true if the specified warning has been suppressed.
	 * @param warning
	 * @return
	 */
	public boolean isWarningSuppressed(SuppressWarning warning) {
		return suppressWarnings.contains(warning);
	}

	/**
	 * Returns true if the specified compiler option is present.
	 * @param option
	 * @return
	 */
	public boolean hasCompilerOption(CompilerOption option) {
		return compilerOptions.contains(option);
	}

	/**
	 * Gets the file name. If the actual file name and this value do not match, this is a compiler warning. The default
	 * is an empty string, which should suppress the warning.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the file author(s). This is not used programmatically, and is only for reference.
	 * @return
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Gets the file creation date. This is not used programmatically, and is only for reference.
	 * @return
	 */
	public String getCreated() {
		return created;
	}

	/**
	 * Gets the file description. This is not used programmatically, and is only for reference.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns whether or not this file has required extensions.
	 * @return
	 */
	public boolean requiresExtensions() {
		return !requiredExtensions.isEmpty();
	}

	/**
	 * Returns the list of required extensions for this file. It should be a compile error if the file requires an
	 * extension, but the extension is not present.
	 * @return
	 */
	public Set<String> getRequiredExtensions() {
		return requiredExtensions;
	}

	/**
	 * Gets the license that this file is released under. This is not used programmatically, and is only for reference.
	 * @return
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * Gets the copyright data that pertains to this file. This is not used programmatically, and is only for reference.
	 * @return
	 */
	public String getCopyright() {
		return copyright;
	}

	/**
	 * The specification for FileOptions states that options that are not recognized are not an error. Given that,
	 * it should be possible to retrieve these unknown options from the list of options. In general, fully supported
	 * options should be accessed using the other methods in this class, but extensions and some other code may benefit
	 * from being able to access the raw options. There is no processing done on this list, so it contains the first
	 * class options as well, in their unparsed format.
	 * @return A copy of the raw options Map.
	 */
	public Map<String, String> getRawOptions() {
		return new HashMap<>(rawOptions);
	}

	/**
	 * Gets a list of known options, that should be used for things like documentation, syntax highlighting, etc.
	 * @return
	 */
	public static List<String> getKnownOptions() {
		List<String> list = new ArrayList<>();
		for(Field f : ClassDiscovery.getDefaultInstance().loadFieldsWithAnnotation(Option.class)) {
			list.add(f.getName());
		}
		return list;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Field f : ClassDiscovery.getDefaultInstance().loadFieldsWithAnnotation(Option.class)) {
			String desc = f.getAnnotation(Option.class).value();
			try {
				b.append(desc).append(": ").append(f.get(this));
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				// uh
			}
		}
		return b.toString();
	}

	public static enum CompilerOption implements Documentation {
		AllowAmbiguousCommands("Disables compiler validation for ambigous commands (only applicable to MSA files).",
			MSVersion.V3_3_4),
		UltraStrict("Provides an extra strict programming environment. Nitpicky details may be covered in ultra strict"
				+ " mode, and will turn almost all warnings into compiler errors. This will also apply all lint"
				+ " settings that would be warnings into errors as well, and is generally the most pedantic"
				+ " version of strict mode available. This is used in native code, but is not necessarily recommened,"
				+ " since it offers no flexibility, however, code that passes ultra strict mode will generally be"
				+ " considered \"ideal\" code, and enshrines the standard code layout. Warnings that"
				+ " are explicitely suppressed are not errors in this mode.", MSVersion.V3_3_4);

		private final String docs;
		private final Version version;

		private CompilerOption(String docs, Version version) {
			this.docs = docs;
			this.version = version;
		}

		@Override
		public URL getSourceJar() {
			return ClassDiscovery.GetClassContainer(this.getClass());
		}

		@Override
		public Class<? extends Documentation>[] seeAlso() {
			return new Class[]{};
		}

		@Override
		public String getName() {
			return this.name();
		}

		@Override
		public String docs() {
			return docs;
		}

		@Override
		public Version since() {
			return version;
		}

	}

	public static enum SuppressWarning implements Documentation {
		// In the future, when some are added, this can be removed, and the rest of the system will work
		// quite nicely. Perhaps a good first candidate would be to allow string "false" coerced to boolean warning
		// to be suppressed on a per file basis?
		Note("There are currently no warning suppressions defined, but some will be added in the future",
			MSVersion.V0_0_0);

		private SuppressWarning(String docs, Version version) {
			this.docs = docs;
			this.version = version;
		}

		private final String docs;
		private final Version version;
		@Override
		public URL getSourceJar() {
			return ClassDiscovery.GetClassContainer(this.getClass());
		}

		@Override
		public Class<? extends Documentation>[] seeAlso() {
			return new Class[]{};
		}

		@Override
		public String getName() {
			return this.name();
		}

		@Override
		public String docs() {
			return docs;
		}

		@Override
		public Version since() {
			return version;
		}
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Option {
		/**
		 * The human readable description of the option
		 * @return
		 */
		String value();
	}

}
