package edu.harvard.iq.dataverse.externaltools;

import java.io.Serializable;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A specification or definition for how an external tool is intended to
 * operate. The specification is applied dynamically on a per-file basis through
 * an {@link ExternalToolHandler}.
 */
@Entity
public class ExternalTool implements Serializable {

    public static final String DISPLAY_NAME = "displayName";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";
    public static final String SCOPE = "scope";
    public static final String TOOL_URL = "toolUrl";
    public static final String TOOL_PARAMETERS = "toolParameters";
    public static final String CONTENT_TYPE = "contentType";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The display name (on the button, for example) of the tool in English.
     */
    // TODO: How are we going to internationalize the display name?
    @Column(nullable = false)
    private String displayName;

    /**
     * The description of the tool in English.
     */
    // TODO: How are we going to internationalize the description?
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the tool is an "explore" tool or a "configure" tool, for example.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    /**
     * Whether the tool operates at the dataset or file level.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Column(nullable = false)
    private String toolUrl;

    /**
     * Parameters the tool requires such as DataFile id and API Token as a JSON
     * object, persisted as a String.
     */
    @Column(nullable = false)
    private String toolParameters;

    /**
     * The file content type the tool works on. For tabular files, the type
     * text/tab-separated-values should be sent
     */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String contentType;

    /**
     * This default constructor is only here to prevent this error at
     * deployment:
     *
     * Exception Description: The instance creation method
     * [...ExternalTool.<Default Constructor>], with no parameters, does not
     * exist, or is not accessible
     *
     * Don't use it.
     */
    @Deprecated
    public ExternalTool() {
    }

    public ExternalTool(String displayName, String description, Type type, Scope scope, String toolUrl, String toolParameters, String contentType) {
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.scope = scope;
        this.toolUrl = toolUrl;
        this.toolParameters = toolParameters;
        this.contentType = contentType;
    }

    public enum Type {

        EXPLORE("explore"),
        CONFIGURE("configure");

        private final String text;

        private Type(final String text) {
            this.text = text;
        }

        public static Type fromString(String text) {
            if (text != null) {
                for (Type type : Type.values()) {
                    if (text.equals(type.text)) {
                        return type;
                    }
                }
            }
            throw new IllegalArgumentException("Type must be one of these values: " + Arrays.asList(Type.values()) + ".");
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum Scope {

        DATASET("dataset"),
        FILE("file");

        private final String text;

        private Scope(final String text) {
            this.text = text;
        }

        public static Scope fromString(String text) {
            if (text != null) {
                for (Scope scope : Scope.values()) {
                    if (text.equals(scope.text)) {
                        return scope;
                    }
                }
            }
            throw new IllegalArgumentException("Scope must be one of these values: " + Arrays.asList(Scope.values()) + ".");
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public Scope getScope() {
        return scope;
    }

    public String getToolUrl() {
        return toolUrl;
    }

    public void setToolUrl(String toolUrl) {
        this.toolUrl = toolUrl;
    }

    public String getToolParameters() {
        return toolParameters;
    }

    public void setToolParameters(String toolParameters) {
        this.toolParameters = toolParameters;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public JsonObjectBuilder toJson() {
        JsonObjectBuilder jab = Json.createObjectBuilder();
        jab.add("id", getId());
        jab.add(DISPLAY_NAME, getDisplayName());
        jab.add(DESCRIPTION, getDescription());
        jab.add(TYPE, getType().text);
        jab.add(SCOPE, getScope().text);
        jab.add(TOOL_URL, getToolUrl());
        jab.add(TOOL_PARAMETERS, getToolParameters());
        if (getContentType() != null) {
            jab.add(CONTENT_TYPE, getContentType());
        }
        return jab;
    }

    public enum ReservedWord {

        // TODO: Research if a format like "{reservedWord}" is easily parse-able or if another format would be
        // better. The choice of curly braces is somewhat arbitrary, but has been observed in documenation for
        // various REST APIs. For example, "Variable substitutions will be made when a variable is named in {brackets}."
        // from https://swagger.io/specification/#fixed-fields-29 but that's for URLs.
        FILE_ID("fileId"),
        FILE_PID("filePid"),
        SITE_URL("siteUrl"),
        API_TOKEN("apiToken"),
        // datasetId is the database id
        DATASET_ID("datasetId"),
        // datasetPid is the DOI or Handle
        DATASET_PID("datasetPid"),
        DATASET_VERSION("datasetVersion"),
        FILE_METADATA_ID("fileMetadataId");

        private final String text;
        private final String START = "{";
        private final String END = "}";

        private ReservedWord(final String text) {
            this.text = START + text + END;
        }

        /**
         * This is a centralized method that enforces that only reserved words
         * are allowed to be used by external tools. External tool authors
         * cannot pass their own query parameters through Dataverse such as
         * "mode=mode1".
         *
         * @throws IllegalArgumentException
         */
        public static ReservedWord fromString(String text) throws IllegalArgumentException {
            if (text != null) {
                for (ReservedWord reservedWord : ReservedWord.values()) {
                    if (text.equals(reservedWord.text)) {
                        return reservedWord;
                    }
                }
            }
            // TODO: Consider switching to a more informative message that enumerates the valid reserved words.
            boolean moreInformativeMessage = false;
            if (moreInformativeMessage) {
                throw new IllegalArgumentException("Unknown reserved word: " + text + ". A reserved word must be one of these values: " + Arrays.asList(ReservedWord.values()) + ".");
            } else {
                throw new IllegalArgumentException("Unknown reserved word: " + text);
            }
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
