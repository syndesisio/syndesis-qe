package io.syndesis.qe.bdd.datamapper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.json.v2.InspectionType;
import io.atlasmap.json.v2.JsonDataSource;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Properties;
import io.atlasmap.xml.v2.XmlDataSource;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.qe.bdd.entities.DataMapperStepDefinition;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.endpoints.AtlasmapEndpoint;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 1, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Component
public class AtlasMapperGenerator {

    @Autowired
    private AtlasmapEndpoint atlasmapEndpoint;

    public AtlasMapperGenerator() {
    }

    /**
     * Using output datashape, generates jsonInspectionResponse for steps preceding atlasMapping we want to generate. In
     * case, the specification is of JavaClass-type, is only transforms this scpecification into required Field listing.
     * The jsonInspectionResponse is stored in StepDefinition.
     *
     * @param precedingSteps
     */
    private void processPrecedingSteps(List<StepDefinition> precedingSteps) {
        for (StepDefinition s : precedingSteps) {
            String stepSpecification = s.getConnectorDescriptor().get().getOutputDataShape().get().getSpecification();
            DataShapeKinds dsKind = s.getConnectorDescriptor().get().getOutputDataShape().get().getKind();
            s.setInspectionResponseFields(Optional.ofNullable(processDataShapeIntoFields(stepSpecification, dsKind)));
        }
    }

    /**
     * Using input datashape, generates jsonInspectionResponse for step following after atlasMapping we want to
     * generate. The jsonInspectionResponse is stored in StepDefinition.
     *
     * @param followingStep
     */
    private void processFolowingStep(StepDefinition followingStep) {
        String stepSpecification = followingStep.getConnectorDescriptor().get().getInputDataShape().get().getSpecification();
        DataShapeKinds dsKind = followingStep.getConnectorDescriptor().get().getInputDataShape().get().getKind();
        followingStep.setInspectionResponseFields(Optional.ofNullable(processDataShapeIntoFields(stepSpecification, dsKind)));
    }

    /**
     * The dataShapeSpecification needs to be separated into fields, which could then be used for generation of mapping
     * steps. This is different for the "Json", "Java" and also "Xml" data shape type. Java DS type is already the
     * specification of field types, Json needs to be send to be sent first to Json inspection endpoint, to generate the
     * fields. Support for XML will be added later.
     *
     * @param dataShapeSpecification
     * @param dsKind
     * @return
     */
    private List<Field> processDataShapeIntoFields(String dataShapeSpecification, DataShapeKinds dsKind) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        List<Field> fields = null;
        log.debug(dataShapeSpecification);

        if (dsKind.equals(DataShapeKinds.JAVA)) {
            try {
                JavaClass jClass = mapper.readValue(dataShapeSpecification, JavaClass.class);
                jClass = mapper.readValue(dataShapeSpecification, JavaClass.class);
                List<JavaField> jfields = getJavaFields(jClass);
                fields = jfields.stream().map(f -> (Field) f).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("error: {}" + e);
            }
        } else if (dsKind.equals(DataShapeKinds.JSON_SCHEMA) || dsKind.equals(DataShapeKinds.JSON_INSTANCE)) {
            JsonInspectionResponse inspectionResponse = atlasmapEndpoint.inspectJson(generateJsonInspectionRequest(dataShapeSpecification));
            try {
                String mapperString = mapper.writeValueAsString(inspectionResponse);
                log.debug(mapperString);
                fields = inspectionResponse.getJsonDocument().getFields().getField();
            } catch (JsonProcessingException e) {
                log.error("error: {}" + e);
            }
        } else if (dsKind.equals(DataShapeKinds.XML_SCHEMA) || dsKind.equals(DataShapeKinds.XML_INSTANCE)) {
            //TODO(tplevko)
            throw new UnsupportedOperationException("XML support is not implemented yet");
        }
        return fields;
    }

    /**
     * Gets list of output data shapes for preceding steps.
     *
     * @param precedingSteps
     * @return
     */
    private List<DataSource> processSources(List<StepDefinition> precedingSteps) {
        List<DataSource> sources = new ArrayList<>();
        for (StepDefinition s : precedingSteps) {
            DataShape outDataShape = s.getConnectorDescriptor().get().getOutputDataShape().get();
            sources.add(createDataSource(outDataShape, s, DataSourceType.SOURCE));
        }
        return sources;
    }

    /**
     * Gets input data shape for specified step, which should follow after the mapping.
     *
     * @param followingStep
     * @return
     */
    private DataSource processTarget(StepDefinition followingStep) {
        DataSource target = null;
        DataShape inDataShape = followingStep.getConnectorDescriptor().get().getInputDataShape().get();
        target = createDataSource(inDataShape, followingStep, DataSourceType.TARGET);
        return target;
    }

    /**
     * Used to generate data source elements for atlasMapping. There are three types of them: Java, Json, XML.
     *
     * TODO(tplevko): update also for XML
     *
     * @param dataShape
     * @param step
     * @param dataSourceType
     * @return
     */
    private DataSource createDataSource(DataShape dataShape, StepDefinition step, DataSourceType dataSourceType) {
        DataShapeKinds dataShapeKind = dataShape.getKind();

        DataSource source = null;

        if (dataShapeKind.toString().contains("json")) {
            source = new JsonDataSource();
            source.setUri("atlas:" + "json:" + step.getStep().getId().get());
        } else if (dataShapeKind.toString().contains("java")) {
            source = new DataSource();
            source.setUri("atlas:" + "java:" + step.getStep().getId().get() + "?className=" + dataShape.getType());
        } else if (dataShapeKind.toString().contains("xml")) {
            source = new XmlDataSource();
            //TODO(tplevko): find out how should look the XML datasource definition
        }
        source.setId(step.getStep().getId().get());
        source.setDataSourceType(dataSourceType);
        return source;
    }

    /**
     * This method is used to generate the "AtlasMapping" - the atlasMapping contains list of specifications of
     * dataSources and a list of specifications of dataMappings. Both these a must have for a complete and working
     * AtlasMapping.
     *
     * @param mapping
     * @param precedingSteps
     * @param followingStep
     * @return
     */
    public Step getAtlasMappingStep(StepDefinition mapping, List<StepDefinition> precedingSteps, StepDefinition followingStep) {

        processPrecedingSteps(precedingSteps);
        processFolowingStep(followingStep);
        List<DataMapperStepDefinition> mappings = mapping.getDataMapperDefinition().get().getDataMapperStepDefinition();

        AtlasMapping atlasMapping = new AtlasMapping();
        atlasMapping.setMappings(new Mappings());

        for (DataSource s : processSources(precedingSteps)) {
            atlasMapping.getDataSource().add(s);
        }

        atlasMapping.setName("REST." + UUID.randomUUID().toString());
        atlasMapping.setLookupTables(new LookupTables());
        atlasMapping.setProperties(new Properties());
        atlasMapping.getDataSource().add(processTarget(followingStep));
        atlasMapping.getMappings().getMapping().addAll(generateBaseMappings(mappings, precedingSteps, followingStep));

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        String mapperString = null;
        try {
            mapperString = mapper.writeValueAsString(atlasMapping);
            log.debug(mapperString);
        } catch (JsonProcessingException e) {
            log.error("error: {}" + e);
        }

        final Step mapperStep = new Step.Builder()
                .stepKind(StepKind.mapper)
                .name(mapping.getStep().getName())
                .configuredProperties(TestUtils.map("atlasmapping", mapperString))
                .action(getMapperStepAction(followingStep.getConnectorDescriptor().get()))
                .id(UUID.randomUUID().toString())
                .build();

        return mapperStep;
    }

    /**
     * Generates a list of mappings - using the user-specified "DataMapperStepDefinition" mapping step definitions.
     *
     * @param mappings
     * @param precedingSteps
     * @param followingStep
     * @return
     */
    private List<BaseMapping> generateBaseMappings(List<DataMapperStepDefinition> mappings, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        List<BaseMapping> baseMapping = new ArrayList<>();

        for (DataMapperStepDefinition m : mappings) {
            baseMapping.add(generateMapping(m, precedingSteps, followingStep));
        }
        return baseMapping;
    }

    /**
     * Determines the MappingType and determines which kind of mapping step it should generate (MAP, COMBINE, SEPARATE).
     *
     * @param mappingDef
     * @param precedingSteps
     * @param followingStep
     * @return
     */
    private Mapping generateMapping(DataMapperStepDefinition mappingDef, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        Mapping generatedMapping = null;
        if (mappingDef.getMappingType().equals(MappingType.MAP)) {
            generatedMapping = generateMapMapping(mappingDef, precedingSteps, followingStep);
        } else if (mappingDef.getMappingType().equals(MappingType.COMBINE)) {
            generatedMapping = generateCombineMapping(mappingDef, precedingSteps, followingStep);
        } else if (mappingDef.getMappingType().equals(MappingType.SEPARATE)) {
            generatedMapping = generateSeparateMapping(mappingDef, precedingSteps, followingStep);
        } else {
            throw new UnsupportedOperationException("The specified operation is not yet supported");
        }
        return generatedMapping;
    }

    /**
     * This method generates "combine" mapping - takes list of input fields and with specified SeparatorType - creates
     * single output field.
     *
     * @param mappingDef - definition of mapping step obtained in step definition
     * @param precedingSteps - all steps preceeding the output step
     * @param followingStep - single step, to which the mapping is applied
     * @return
     */
    private Mapping generateCombineMapping(DataMapperStepDefinition mappingDef, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        StepDefinition fromStep = precedingSteps.get(mappingDef.getFromStep() - 1);

        Mapping generatedMapping = new Mapping();
        generatedMapping.setId(UUID.randomUUID().toString());
        generatedMapping.setMappingType(MappingType.COMBINE);
        generatedMapping.setDelimiter(mappingDef.getStrategy().name());

        List<Field> in = new ArrayList<>();

        for (int i = 0; i < mappingDef.getInputFields().size(); i++) {
            String def = mappingDef.getInputFields().get(i);
            Field inField = fromStep.getInspectionResponseFields().get()
                    .stream().filter(f -> f.getPath().matches(def)).findFirst().get();
            inField.setIndex(i);
            in.add(inField);
        }

        Field out = followingStep.getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(mappingDef.getOutputFields().get(0))).findFirst().get();

        in.forEach(f -> f.setDocId(fromStep.getStep().getId().get()));
        out.setDocId(followingStep.getStep().getId().get());

        generatedMapping.getInputField().addAll(in);
        generatedMapping.getOutputField().add(out);
        return generatedMapping;
    }

    /**
     * This method generates "separate" mapping - takes single input field and using specified SeparatorType (coma,
     * space, ...) - creates multiple output fields
     *
     * @param mappingDef - definition of mapping step obtained in step definition
     * @param precedingSteps - all steps preceeding the output step
     * @param followingStep - single step, to which the mapping is applied
     * @return
     */
    private Mapping generateSeparateMapping(DataMapperStepDefinition mappingDef, List<StepDefinition> precedingSteps, StepDefinition followingStep) {
        StepDefinition fromStep = precedingSteps.get(mappingDef.getFromStep() - 1);

        Mapping generatedMapping = new Mapping();
        generatedMapping.setId(UUID.randomUUID().toString());
        generatedMapping.setMappingType(MappingType.SEPARATE);
        generatedMapping.setDelimiter(mappingDef.getStrategy().name());

        List<Field> out = new ArrayList<>();

        for (int i = 0; i < mappingDef.getOutputFields().size(); i++) {
            String def = mappingDef.getOutputFields().get(i);
            Field outField = followingStep.getInspectionResponseFields().get()
                    .stream().filter(f -> f.getPath().matches(def)).findFirst().get();
            outField.setIndex(i);
            out.add(outField);
        }
        Field in = fromStep.getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(mappingDef.getInputFields().get(0))).findFirst().get();

        out.forEach(f -> f.setDocId(followingStep.getStep().getId().get()));
        in.setDocId(fromStep.getStep().getId().get());

        generatedMapping.getOutputField().addAll(out);
        generatedMapping.getInputField().add(in);
        return generatedMapping;
    }

    /**
     * This method generates "map" type mapping - takes single input field and creates single output field
     *
     * @param mappingDef - definition of mapping step obtained in step definition
     * @param precedingSteps - all steps preceeding the output step
     * @param followingStep - single step, to which the mapping is applied
     * @return
     */
    private Mapping generateMapMapping(DataMapperStepDefinition mappingDef, List<StepDefinition> precedingSteps, StepDefinition followingStep) {

        StepDefinition fromStep = precedingSteps.get(mappingDef.getFromStep() - 1);

        Mapping generatedMapping = new Mapping();
        generatedMapping.setId(UUID.randomUUID().toString());
        generatedMapping.setMappingType(MappingType.MAP);

        Field in = fromStep.getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(mappingDef.getInputFields().get(0))).findFirst().get();

        Field out = followingStep.getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(mappingDef.getOutputFields().get(0))).findFirst().get();

        in.setDocId(fromStep.getStep().getId().get());
        out.setDocId(followingStep.getStep().getId().get());

        generatedMapping.getInputField().add(in);
        generatedMapping.getOutputField().add(out);
        return generatedMapping;
    }

    /**
     * Creates JsonInspectionRequest object out of specified datashape specification.
     *
     * @param specification
     * @return
     */
    private JsonInspectionRequest generateJsonInspectionRequest(String specification) {

        log.debug(specification);

        JsonInspectionRequest jsonInspectReq = new JsonInspectionRequest();
        jsonInspectReq.setJsonData(specification);
        jsonInspectReq.setType(InspectionType.SCHEMA);

        return jsonInspectReq;
    }

    /**
     * Generates the list of JavaFields from nested list of JavaClasses and JavaFields.
     *
     * @param jClass
     * @return
     */
    private List<JavaField> getJavaFields(JavaClass jClass) {
        List<JavaField> fields = jClass.getJavaFields().getJavaField();
        List<JavaField> javaField = new ArrayList<>();
        for (JavaField jf : fields) {
            if (jf instanceof JavaClass) {
                javaField.addAll(getJavaFields((JavaClass) jf));
            } else if (jf instanceof JavaField) {
                javaField.add(jf);
            }
        }
        return javaField;
    }

    /**
     * Small hack - generated Mapper step action - this specification needs the input and output data shape. The input
     * is "DataShapeKin.ANY" - marks it takes all the preceeding dataOutputShapes from preceeding steps. The
     * outputDataShape corresponds to the inputDataShape of the following step.
     *
     * @param outputConnectorDescriptor
     * @return
     */
    public Action getMapperStepAction(ConnectorDescriptor outputConnectorDescriptor) {
        ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        Action ts = new StepAction.Builder().descriptor(new StepDescriptor.Builder().build()).build();
        try {
            DataShape inputDataShape = new DataShape.Builder().kind(DataShapeKinds.ANY).name("All preceding outputs").build();
            JSONObject json = new JSONObject(mapper.writeValueAsString(ts));
            JSONObject inputDataType = new JSONObject(mapper.writeValueAsString(inputDataShape));
            JSONObject outputDataType = new JSONObject(mapper.writeValueAsString(outputConnectorDescriptor.getInputDataShape().get()));

            json.getJSONObject("descriptor").put("inputDataShape", inputDataType);
            json.getJSONObject("descriptor").put("outputDataShape", outputDataType);
            ts = Json.reader().forType(Action.class).readValue(json.toString());
            log.debug(mapper.writeValueAsString(ts));

        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return ts;
    }
}
