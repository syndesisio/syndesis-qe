package io.syndesis.qe.datamapper;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.qe.endpoint.AtlasmapEndpoint;
import io.syndesis.qe.entities.DataMapperStepDefinition;
import io.syndesis.qe.entities.StepDefinition;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.lang3.StringUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDataSource;
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
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import io.atlasmap.xml.v2.XmlNamespaces;
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

    private StepDefinition mapping;
    private List<StepDefinition> precedingSteps;
    private StepDefinition followingStep;

    public void setSteps(StepDefinition mappingSd, List<StepDefinition> precedingStepsList, StepDefinition followingStepSd) {
        this.mapping = mappingSd;
        this.precedingSteps = precedingStepsList;
        this.followingStep = followingStepSd;
    }

    /**
     * Using output datashape, generates jsonInspectionResponse for steps preceding atlasMapping we want to generate. In
     * case, the specification is of JavaClass-type, is only transforms this scpecification into required Field listing.
     * The jsonInspectionResponse is stored in StepDefinition.
     */
    private void processPrecedingSteps() {
        precedingSteps.stream().filter(s -> s.getStep().getAction().isPresent()).forEach(s -> {
            String stepSpecification = s.getStep().getAction().get().getOutputDataShape().get().getSpecification();
            DataShapeKinds dsKind = s.getStep().getAction().get().getOutputDataShape().get().getKind();
            s.setInspectionResponseFields(Optional.ofNullable(processDataShapeIntoFields(stepSpecification, dsKind)));
        });
    }

    /**
     * Using input datashape, generates jsonInspectionResponse for step following after atlasMapping we want to
     * generate. The jsonInspectionResponse is stored in StepDefinition.
     */
    private void processFollowingStep() {
        String stepSpecification = followingStep.getStep().getAction().get().getInputDataShape().get().getSpecification();
        DataShapeKinds dsKind = followingStep.getStep().getAction().get().getInputDataShape().get().getKind();
        followingStep.setInspectionResponseFields(Optional.ofNullable(processDataShapeIntoFields(stepSpecification, dsKind)));
    }

    /**
     * The dataShapeSpecification needs to be separated into fields, which could then be used for generation of mapping
     * steps. This is different for the "Json", "Java" and also "Xml" data shape type. Java DS type is already the
     * specification of field types, Json needs to be send to be sent first to Json inspection endpoint, to generate the
     * fields.
     *
     * @return list of fields from given datashape
     */
    private List<Field> processDataShapeIntoFields(String dataShapeSpecification, DataShapeKinds dsKind) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        List<Field> fields = null;

        if (dsKind.equals(DataShapeKinds.JAVA)) {
            try {
                JavaClass jClass = mapper.readValue(dataShapeSpecification, JavaClass.class);
                fields = getJavaFields(jClass).stream().map(this::replacePrimitiveWithObject).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("error: ", e);
            }
        } else if (dsKind.equals(DataShapeKinds.JSON_SCHEMA) || dsKind.equals(DataShapeKinds.JSON_INSTANCE)) {
            JsonInspectionResponse inspectionResponse = atlasmapEndpoint.inspectJson(dataShapeSpecification, dsKind);
            try {
                log.debug("Inspection API response: " + mapper.writeValueAsString(inspectionResponse));
                fields = inspectionResponse.getJsonDocument().getFields().getField();
            } catch (JsonProcessingException e) {
                log.error("Unable to write inspection API response as string", e);
            }
        } else if (dsKind.equals(DataShapeKinds.XML_SCHEMA) || dsKind.equals(DataShapeKinds.XML_INSTANCE)) {
            XmlInspectionResponse inspectionResponse = atlasmapEndpoint.inspectXml(dataShapeSpecification, dsKind);
            try {
                log.debug("Inspection API response: " + mapper.writeValueAsString(inspectionResponse));
                fields = inspectionResponse.getXmlDocument().getFields().getField();
            } catch (JsonProcessingException e) {
                log.error("Unable to write inspection API response as string", e);
            }
        }
        return fields;
    }

    /**
     * Converts the className and canonicalClassName from primitives to object,
     * because otherwise it would fail at runtime with: class "int" not found on classpath.
     *
     * @param f java field
     * @return atlasmap field
     */
    private Field replacePrimitiveWithObject(JavaField f) {
        switch (f.getClassName()) {
            case "boolean":
            case "byte":
            case "double":
            case "float":
            case "long":
            case "short":
                f.setClassName("java.lang." + StringUtils.capitalize(f.getClassName()));
                f.setCanonicalClassName("java.lang." + StringUtils.capitalize(f.getClassName()));
                break;
            case "char":
                f.setClassName("java.lang.Character");
                f.setCanonicalClassName("java.lang.Character");
                break;
            case "int":
                f.setClassName("java.lang.Integer");
                f.setCanonicalClassName("java.lang.Integer");
        }
        return (Field) f;
    }

    /**
     * Gets list of output data shapes for preceding steps.
     *
     * @return list of datasources from preceding steps
     */
    private List<DataSource> processSources() {
        List<DataSource> sources = new ArrayList<>();
        precedingSteps.stream().filter(s -> s.getStep().getAction().isPresent()).forEach(s -> {
            DataShape outDataShape = s.getStep().getAction().get().getOutputDataShape().get();
            // Steps with "ANY" or "NONE" are ignored for sources and only those that have proper datashape are used
            if (outDataShape.getKind() != DataShapeKinds.ANY && outDataShape.getKind() != DataShapeKinds.NONE) {
                sources.add(createDataSource(outDataShape, s, DataSourceType.SOURCE));
            }
        });
        return sources;
    }

    /**
     * Gets input data shape for specified step, which should follow after the mapping.
     *
     * @return datasource for the target step
     */
    private DataSource processTarget() {
        DataShape inDataShape = followingStep.getStep().getAction().get().getInputDataShape().get();
        if (inDataShape.getKind() == DataShapeKinds.ANY) {
            fail("Unable to map to \"ANY\" datashape!");
        }
        return createDataSource(inDataShape, followingStep, DataSourceType.TARGET);
    }

    /**
     * Used to generate data source elements for atlasMapping. There are three types of them: Java, Json, XML.
     *
     * @param dataShape datashape of the processed step
     * @param step step definition
     * @param dataSourceType datasource type
     * @return datasource
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
            source.setUri("atlas:xml:" + step.getStep().getId().get());
            XmlNamespaces xmlNamespaces = new XmlNamespaces();
            // Init the array, so that we don't have the null value
            xmlNamespaces.getXmlNamespace();
            ((XmlDataSource) source).setXmlNamespaces(xmlNamespaces);
        } else {
            fail("Unknown datashape kind " + dataShapeKind.toString());
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
     * @return step with the mapping defined
     */
    public Step getAtlasMappingStep() {
        processPrecedingSteps();
        processFollowingStep();

        AtlasMapping atlasMapping = new AtlasMapping();
        atlasMapping.setMappings(new Mappings());

        for (DataSource s : processSources()) {
            atlasMapping.getDataSource().add(s);
        }

        atlasMapping.setName("REST." + UUID.randomUUID().toString().replaceAll("-", ""));
        atlasMapping.setLookupTables(new LookupTables());
        atlasMapping.setProperties(new Properties());
        atlasMapping.getDataSource().add(processTarget());
        atlasMapping.getMappings().getMapping().addAll(generateBaseMappings());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        String mapperString = null;
        try {
            mapperString = mapper.writeValueAsString(atlasMapping);
            log.debug(mapperString);
        } catch (JsonProcessingException e) {
            log.error("Unable to write mapper json as string", e);
        }

        return new Step.Builder()
            .stepKind(StepKind.mapper)
            .name(mapping.getStep().getName())
            .configuredProperties(TestUtils.map("atlasmapping", mapperString))
            .action(getMapperStepAction(followingStep.getStep().getAction().get().getInputDataShape().get()))
            .id(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Generates a list of mappings - using the user-specified "DataMapperStepDefinition" mapping step definitions.
     *
     * @return list of basemapping
     */
    private List<BaseMapping> generateBaseMappings() {
        List<BaseMapping> baseMapping = new ArrayList<>();

        for (DataMapperStepDefinition m : mapping.getDataMapperDefinition().get().getDataMapperStepDefinition()) {
            baseMapping.add(generateMapping(m));
        }
        return baseMapping;
    }

    /**
     * Determines the MappingType and determines which kind of mapping step it should generate (MAP, COMBINE, SEPARATE).
     *
     * @return mapping object
     */
    private Mapping generateMapping(DataMapperStepDefinition mappingDef) {
        Mapping generatedMapping;
        if (mappingDef.getMappingType().equals(MappingType.MAP)) {
            generatedMapping = generateMapMapping(mappingDef);
        } else if (mappingDef.getMappingType().equals(MappingType.COMBINE)) {
            generatedMapping = generateCombineMapping(mappingDef);
        } else if (mappingDef.getMappingType().equals(MappingType.SEPARATE)) {
            generatedMapping = generateSeparateMapping(mappingDef);
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
     * @return mapping object
     */
    private Mapping generateCombineMapping(DataMapperStepDefinition mappingDef) {
        List<Field> in = new ArrayList<>();

        for (int i = 0; i < mappingDef.getInputFields().size(); i++) {
            String def = mappingDef.getInputFields().get(i);
            Field inField = precedingSteps.get(mappingDef.getFromStep() - 1).getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(def)).findFirst().get();
            inField.setIndex(i);
            in.add(inField);
        }

        Field out = getField(followingStep.getInspectionResponseFields().get(), mappingDef.getOutputFields().get(0));
        if (out == null) {
            fail("Unable to find \"out\" field with path " + mappingDef.getOutputFields().get(0));
        }

        return createMappingObject(mappingDef, MappingType.COMBINE, mappingDef.getStrategy().name(), in, Collections.singletonList(out));
    }

    /**
     * This method generates "separate" mapping - takes single input field and using specified SeparatorType (coma,
     * space, ...) - creates multiple output fields
     *
     * @param mappingDef - definition of mapping step obtained in step definition
     * @return mapping object
     */
    private Mapping generateSeparateMapping(DataMapperStepDefinition mappingDef) {
        List<Field> out = new ArrayList<>();

        for (int i = 0; i < mappingDef.getOutputFields().size(); i++) {
            String def = mappingDef.getOutputFields().get(i);
            Field outField = followingStep.getInspectionResponseFields().get()
                .stream().filter(f -> f.getPath().matches(def)).findFirst().get();
            outField.setIndex(i);
            out.add(outField);
        }
        Field in = getField(precedingSteps.get(mappingDef.getFromStep() - 1).getInspectionResponseFields().get(), mappingDef.getInputFields().get(0));
        if (in == null) {
            fail("Unable to find \"in\" field with path " + mappingDef.getInputFields().get(0));
        }

        return createMappingObject(mappingDef, MappingType.SEPARATE, mappingDef.getStrategy().name(), Collections.singletonList(in), out);
    }

    /**
     * This method generates "map" type mapping - takes single input field and creates single output field
     *
     * @param mappingDef - definition of mapping step obtained in step definition
     * @return mapping object
     */
    private Mapping generateMapMapping(DataMapperStepDefinition mappingDef) {
        Field in = getField(precedingSteps.get(mappingDef.getFromStep() - 1).getInspectionResponseFields().get(), mappingDef.getInputFields().get(0));
        if (in == null) {
            fail("Unable to find \"in\" field with path " + mappingDef.getInputFields().get(0));
        }
        Field out = getField(followingStep.getInspectionResponseFields().get(), mappingDef.getOutputFields().get(0));
        if (out == null) {
            fail("Unable to find \"out\" field with path " + mappingDef.getOutputFields().get(0));
        }
        if (System.getProperty("sqs.batch") != null) {
            // sqs batch needs to be mapped to "/message", however the inspect endpoints returns "/<>/message", because internally it is a list of
            // messages
            // so hack it and change to single element
            out.setPath(StringUtils.substringAfter(out.getPath(), "/<>"));
        }

        return createMappingObject(mappingDef, MappingType.MAP, null, Collections.singletonList(in), Collections.singletonList(out));
    }

    /**
     * Creates the mapping object from given properties.
     *
     * @param mappingDef mapping definition
     * @param type mapping type
     * @param delimiter delimiter for combine/separate
     * @param in list of source fields
     * @param out list of target fields
     * @return mapping object
     */
    private Mapping createMappingObject(DataMapperStepDefinition mappingDef, MappingType type, String delimiter,
        List<Field> in, List<Field> out) {
        Mapping generatedMapping = new Mapping();
        generatedMapping.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        generatedMapping.setMappingType(type);
        if (delimiter != null) {
            generatedMapping.setDelimiter(delimiter);
        }

        in.forEach(f -> f.setDocId(precedingSteps.get(mappingDef.getFromStep() - 1).getStep().getId().get()));
        out.forEach(f -> f.setDocId(followingStep.getStep().getId().get()));

        addTransformations(mappingDef, in, out);

        generatedMapping.getInputField().addAll(in);
        generatedMapping.getOutputField().addAll(out);
        return generatedMapping;
    }

    /**
     * Adds transformations to the source/target fields.
     *
     * @param mappingDef mapping definition
     * @param in source fields
     * @param out target fields
     */
    private void addTransformations(DataMapperStepDefinition mappingDef, List<Field> in, List<Field> out) {
        if (mappingDef.getTransformations().get("source") != null && !mappingDef.getTransformations().get("source").isEmpty()) {
            mappingDef.getTransformations().get("source").forEach((id, transformations) -> {
                ArrayList<io.atlasmap.v2.Action> actions = new ArrayList<>();
                transformations.forEach(t -> actions.add((io.atlasmap.v2.Action) t));
                getField(in, id).setActions(actions);
            });
        }

        if (mappingDef.getTransformations().get("target") != null && !mappingDef.getTransformations().get("target").isEmpty()) {
            mappingDef.getTransformations().get("target").forEach((id, transformations) -> {
                ArrayList<io.atlasmap.v2.Action> actions = new ArrayList<>();
                transformations.forEach(t -> actions.add((io.atlasmap.v2.Action) t));
                getField(out, id).setActions(actions);
            });
        }
    }

    /**
     * Gets the field with the path corresponding to the search string. It recursively goes through child fields if the field is a complex type.
     *
     * @param fields list of fields
     * @param searchString path search string
     * @return field
     */
    private Field getField(List<Field> fields, String searchString) {
        Field f = null;
        for (Field field : fields) {
            if (field instanceof JsonComplexType || field instanceof XmlComplexType) {
                // Search child elements of the complex element, just create a new list because Json/XmlField is a child class of Field
                final List<Field> fieldList;
                if (field instanceof JsonComplexType) {
                    fieldList = new ArrayList<>(((JsonComplexType) field).getJsonFields().getJsonField());
                } else {
                    fieldList = new ArrayList<>(((XmlComplexType) field).getXmlFields().getXmlField());
                }
                f = getField(fieldList, searchString);
            } else {
                if (field.getPath().equals(searchString)) {
                    return field;
                } else {
                    continue;
                }
            }

            if (f != null) {
                break;
            }
        }
        return f;
    }

    /**
     * Generates the list of JavaFields from nested list of JavaClasses and JavaFields.
     *
     * @param jClass java class
     * @return list of fields in given class
     */
    private List<JavaField> getJavaFields(JavaClass jClass) {
        List<JavaField> fields = jClass.getJavaFields().getJavaField();
        List<JavaField> javaField = new ArrayList<>();
        for (JavaField jf : fields) {
            if (jf instanceof JavaClass) {
                javaField.addAll(getJavaFields((JavaClass) jf));
            } else {
                javaField.add(jf);
            }
        }
        return javaField;
    }

    /**
     * Small hack - generated Mapper step action - this specification needs the input and output data shape. The input
     * is "DataShapeKind.ANY" - marks it takes all the preceeding dataOutputShapes from preceeding steps. The
     * outputDataShape corresponds to the inputDataShape of the following step.
     *
     * @param outputConnectorInputDataShape datashape
     * @return action with datashapes set
     */
    private Action getMapperStepAction(DataShape outputConnectorInputDataShape) {
        ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        Action ts = new StepAction.Builder().descriptor(new StepDescriptor.Builder().build()).build();
        try {
            DataShape inputDataShape = new DataShape.Builder().kind(DataShapeKinds.ANY).name("All preceding outputs").build();
            JSONObject json = new JSONObject(mapper.writeValueAsString(ts));
            JSONObject inputDataType = new JSONObject(mapper.writeValueAsString(inputDataShape));
            JSONObject outputDataType = new JSONObject(mapper.writeValueAsString(outputConnectorInputDataShape));

            json.getJSONObject("descriptor").put("inputDataShape", inputDataType);
            json.getJSONObject("descriptor").put("outputDataShape", outputDataType);
            ts = JsonUtils.reader().forType(Action.class).readValue(json.toString());
            log.debug(mapper.writeValueAsString(ts));
        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return ts;
    }
}
