/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.preferences;

import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.opentravel.schemas.stl2developer.Activator;

import com.sabre.schemacompiler.ioc.CompilerExtensionRegistry;
import com.sabre.schemacompiler.task.CommonCompilerTaskOptions;
import com.sabre.schemacompiler.task.CompileAllTaskOptions;

/**
 * <code>CompileAllTaskOptions</code> implementation that provides each conversion to and from
 * Eclipse RCP preferences.
 * 
 * @author S. Livezey
 */
public class CompilerPreferences implements CompileAllTaskOptions {

    private static final String PREFERENCE_PREFIX = "stl2Developer.compilerOptions.";
    private static final String PREF_COMPILER_EXTENSION_ID = PREFERENCE_PREFIX
            + "compilerExtensionId";
    private static final String PREF_COMPILE_SCHEMAS = PREFERENCE_PREFIX + "compileSchemas";
    private static final String PREF_COMPILE_SERVICES = PREFERENCE_PREFIX + "compileServices";
    private static final String PREF_SERVICE_ENDPOINT_URL = PREFERENCE_PREFIX
            + "serviceEndpointUrl";
    private static final String PREF_GENERATE_EXAMPLES = PREFERENCE_PREFIX + "generateExamples";
    private static final String PREF_EXAMPLE_MAX_DETAILS = PREFERENCE_PREFIX
            + "generateMaxDetailsForExamples";
    private static final String PREF_EXAMPLE_MAX_REPEAT = PREFERENCE_PREFIX + "exampleMaxRepeat";
    private static final String PREF_EXAMPLE_MAX_DEPTH = PREFERENCE_PREFIX + "exampleMaxDepth";

    private String compilerExtensionId = CompilerExtensionRegistry.getActiveExtension();
    private boolean compileSchemas = true;
    private boolean compileServices = true;
    private String serviceEndpointUrl = null;
    private boolean generateExamples = true;
    private boolean generateMaxDetailsForExamples = true;
    private int exampleMaxRepeat = 3;
    private int exampleMaxDepth = 2;

    /**
     * Default constructor that assigns default values for all preference settings.
     */
    public CompilerPreferences() {
    }

    /**
     * Constructor that obtains the initial option settings from an Eclipse preference store.
     * 
     * @param preferenceStore
     *            the Eclipse RCP preference store that contains the compiler task settings
     */
    public CompilerPreferences(final IPreferenceStore preferenceStore) {
        loadTaskOptions(preferenceStore);
    }

    /**
     * Loads the compiler preferences from the workbench configuration file (or returns default
     * values if the workbench file is not available).
     * 
     * @return PreferenceStore
     */
    public static IPreferenceStore loadPreferenceStore() {
        // this line cousing problem during loading.
        // if (!OtmRegistry.getMainWindow().hasDisplay())
        // return null; // headless operation
        final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

        if (!preferenceStore.contains(PREF_COMPILER_EXTENSION_ID)) {
            new CompilerPreferences().saveTaskOptions(preferenceStore);
        }
        return preferenceStore;
    }

    /**
     * Loads the task settings from the given preference store into this instance.
     * 
     * @param preferenceStore
     *            the Eclipse RCP preference store that contains the compiler task settings
     */
    public void loadTaskOptions(final IPreferenceStore preferenceStore) {
        if (preferenceStore == null)
            return;
        compilerExtensionId = preferenceStore.getString(PREF_COMPILER_EXTENSION_ID);
        compileSchemas = preferenceStore.getBoolean(PREF_COMPILE_SCHEMAS);
        compileServices = preferenceStore.getBoolean(PREF_COMPILE_SERVICES);
        serviceEndpointUrl = preferenceStore.getString(PREF_SERVICE_ENDPOINT_URL);
        generateExamples = preferenceStore.getBoolean(PREF_GENERATE_EXAMPLES);
        generateMaxDetailsForExamples = preferenceStore.getBoolean(PREF_EXAMPLE_MAX_DETAILS);
        exampleMaxRepeat = preferenceStore.getInt(PREF_EXAMPLE_MAX_REPEAT);
        exampleMaxDepth = preferenceStore.getInt(PREF_EXAMPLE_MAX_DEPTH);
    }

    /**
     * Saves the task settings from this instance into the given preference store.
     * 
     * @param preferenceStore
     *            the Eclipse RCP preference store that contains the compiler task settings
     */
    public void saveTaskOptions(final IPreferenceStore preferenceStore) {
        preferenceStore.setValue(PREF_COMPILER_EXTENSION_ID, (compilerExtensionId == null) ? ""
                : compilerExtensionId);
        preferenceStore.setValue(PREF_COMPILE_SCHEMAS, compileSchemas);
        preferenceStore.setValue(PREF_COMPILE_SERVICES, compileServices);
        preferenceStore.setValue(PREF_SERVICE_ENDPOINT_URL, (serviceEndpointUrl == null) ? ""
                : serviceEndpointUrl);
        preferenceStore.setValue(PREF_GENERATE_EXAMPLES, generateExamples);
        preferenceStore.setValue(PREF_EXAMPLE_MAX_DETAILS, generateMaxDetailsForExamples);
        preferenceStore.setValue(PREF_EXAMPLE_MAX_REPEAT, exampleMaxRepeat);
        preferenceStore.setValue(PREF_EXAMPLE_MAX_DEPTH, exampleMaxDepth);
    }

    /**
     * Returns the compiler extension ID that was selected in the OTA2 compiler preferences.
     * 
     * @return String
     */
    public String getCompilerExtensionId() {
        return "".equals(compilerExtensionId) ? null : compilerExtensionId;
    }

    /**
     * Assigns the compiler extension ID that was selected in the OTA2 compiler preferences.
     * 
     * @param compilerExtensionId
     *            the compiler extension ID to assign
     */
    public void setCompilerExtensionId(final String compilerExtensionId) {
        this.compilerExtensionId = compilerExtensionId;
    }

    /**
     * @see com.sabre.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
     */
    @Override
    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    /**
     * Assigns the option flag indicating that XML schema files should be generated.
     * 
     * @param compileSchemas
     *            the task option value to assign
     */
    public void setCompileSchemas(final boolean compileSchemas) {
        this.compileSchemas = compileSchemas;
    }

    /**
     * @see com.sabre.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
     */
    @Override
    public boolean isCompileServices() {
        return compileServices;
    }

    /**
     * Assigns the option flag indicating that WSDL documents should be generated.
     * 
     * @param compileServices
     *            the task option value to assign
     */
    public void setCompileServices(final boolean compileServices) {
        this.compileServices = compileServices;
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
     */
    @Override
    public String getCatalogLocation() {
        return null; // Not editable as an application compiler preference
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
     */
    @Override
    public String getOutputFolder() {
        return null; // Not editable as an application compiler preference
    }

    /**
     * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
     */
    @Override
    public URL getServiceLibraryUrl() {
        return null; // Not editable as an application compiler preference
    }

    /**
     * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
     */
    @Override
    public String getServiceEndpointUrl() {
        return "".equals(serviceEndpointUrl) ? null : serviceEndpointUrl;
    }

    /**
     * Assigns the base URL for all service endpoints generated in WSDL documents.
     * 
     * @param serviceEndpointUrl
     *            the service endpoint URL to assign
     */
    public void setServiceEndpointUrl(final String serviceEndpointUrl) {
        this.serviceEndpointUrl = serviceEndpointUrl;
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#isGenerateExamples()
     */
    @Override
    public boolean isGenerateExamples() {
        return generateExamples;
    }

    /**
     * Assigns the option flag indicating that example XML documents should be generated.
     * 
     * @param compileRAS
     *            the task option value to assign
     */
    public void setGenerateExamples(final boolean generateExamples) {
        this.generateExamples = generateExamples;
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#isGenerateMaxDetailsForExamples()
     */
    @Override
    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    /**
     * Assigns the flag indicating whether the maximum amount of detail is to be included in
     * generated example data. If false, minimum detail will be generated.
     * 
     * @param generateMaxDetailsForExamples
     *            the boolean flag value to assign
     */
    public void setGenerateMaxDetailsForExamples(final boolean generateMaxDetailsForExamples) {
        this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
    }

    /**
     * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#getExampleContext()
     */
    @Override
    public String getExampleContext() {
        return null; // Not editable as an application compiler preference
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxRepeat()
     */
    @Override
    public Integer getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    /**
     * Assigns the maximum number of times that repeating elements should be displayed in generated
     * example output.
     * 
     * @param exampleMaxRepeat
     *            the max repeat value to assign
     */
    public void setExampleMaxRepeat(final Integer exampleMaxRepeat) {
        this.exampleMaxRepeat = exampleMaxRepeat;
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxDepth()
     */
    @Override
    public Integer getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    /**
     * Assigns the maximum depth that should be included for nested elements in generated example
     * output.
     * 
     * @param exampleMaxDepth
     *            the max depth value to assign
     */
    public void setExampleMaxDepth(final Integer exampleMaxDepth) {
        this.exampleMaxDepth = exampleMaxDepth;
    }

    /**
     * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(com.sabre.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(final CommonCompilerTaskOptions taskOptions) {
        throw new UnsupportedOperationException();
    }

}
