/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.java.jaxrs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.mockito.Answers;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.languages.AbstractJavaJAXRSServerCodegen;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.testutils.ConfigAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class AbstractJavaJAXRSServerCodegenTest {

    private AbstractJavaJAXRSServerCodegen codegen;

    /**
     * In TEST-NG, test class (and its fields) is only constructed once (vs. for every test in Jupiter),
     * using @BeforeMethod to have a fresh codegen mock for each test
     */
    @BeforeMethod
    void mockAbstractCodegen() {
        codegen = mock(
                AbstractJavaJAXRSServerCodegen.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS).useConstructor()
        );
    }

    @Test
    public void convertApiName() {
        Assert.assertEquals(codegen.toApiName("name"), "NameApi");
        Assert.assertEquals(codegen.toApiName("$name"), "NameApi");
        Assert.assertEquals(codegen.toApiName("nam#e"), "NameApi");
        Assert.assertEquals(codegen.toApiName("$another-fake?"), "AnotherFakeApi");
        Assert.assertEquals(codegen.toApiName("fake_classname_tags 123#$%^"), "FakeClassnameTags123Api");
    }

    @Test
    public void testInitialConfigValues() throws Exception {
        codegen.processOpts();

        OpenAPI openAPI = new OpenAPI();
        openAPI.addServersItem(new Server().url("https://api.abcde.xy:8082/v2"));
        codegen.preprocessOpenAPI(openAPI);

        ConfigAssert configAssert = new ConfigAssert(codegen.additionalProperties());
        configAssert.assertValue(CodegenConstants.HIDE_GENERATION_TIMESTAMP, codegen::isHideGenerationTimestamp, Boolean.FALSE);
        configAssert.assertValue(CodegenConstants.MODEL_PACKAGE, codegen::modelPackage, "org.openapitools.model");
        configAssert.assertValue(CodegenConstants.API_PACKAGE, codegen::apiPackage, "org.openapitools.api");
        configAssert.assertValue(CodegenConstants.INVOKER_PACKAGE, codegen::getInvokerPackage, "org.openapitools.api");
        configAssert.assertValue(AbstractJavaJAXRSServerCodegen.SERVER_PORT, "8082");
    }

    @Test
    public void testSettersForConfigValues() throws Exception {
        codegen.setHideGenerationTimestamp(true);
        codegen.setModelPackage("xx.yyyyyyyy.model");
        codegen.setApiPackage("xx.yyyyyyyy.api");
        codegen.setInvokerPackage("xx.yyyyyyyy.invoker");
        codegen.processOpts();

        ConfigAssert configAssert = new ConfigAssert(codegen.additionalProperties());
        configAssert.assertValue(CodegenConstants.HIDE_GENERATION_TIMESTAMP, codegen::isHideGenerationTimestamp, Boolean.TRUE);
        configAssert.assertValue(CodegenConstants.MODEL_PACKAGE, codegen::modelPackage, "xx.yyyyyyyy.model");
        configAssert.assertValue(CodegenConstants.API_PACKAGE, codegen::apiPackage, "xx.yyyyyyyy.api");
        configAssert.assertValue(CodegenConstants.INVOKER_PACKAGE, codegen::getInvokerPackage, "xx.yyyyyyyy.invoker");
    }

    @Test
    public void testAdditionalPropertiesPutForConfigValues() throws Exception {
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, "true");
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xyz.yyyyy.mmmmm.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xyz.yyyyy.aaaaa.api");
        codegen.additionalProperties().put(CodegenConstants.INVOKER_PACKAGE, "xyz.yyyyy.iiii.invoker");
        codegen.additionalProperties().put("serverPort", "8088");
        codegen.processOpts();

        OpenAPI openAPI = new OpenAPI();
        openAPI.addServersItem(new Server().url("https://api.abcde.xy:8082/v2"));
        codegen.preprocessOpenAPI(openAPI);
        ConfigAssert configAssert = new ConfigAssert(codegen.additionalProperties());
        configAssert.assertValue(CodegenConstants.HIDE_GENERATION_TIMESTAMP, codegen::isHideGenerationTimestamp, Boolean.TRUE);
        configAssert.assertValue(CodegenConstants.MODEL_PACKAGE, codegen::modelPackage, "xyz.yyyyy.mmmmm.model");
        configAssert.assertValue(CodegenConstants.API_PACKAGE, codegen::apiPackage, "xyz.yyyyy.aaaaa.api");
        configAssert.assertValue(CodegenConstants.INVOKER_PACKAGE, codegen::getInvokerPackage, "xyz.yyyyy.iiii.invoker");
        configAssert.assertValue(AbstractJavaJAXRSServerCodegen.SERVER_PORT, "8088");
    }

    @Test
    public void testCommonPath() {
        OperationsMap objs = new OperationsMap();
        OperationMap opMap = new OperationMap();
        List<CodegenOperation> operations = new ArrayList<>();
        objs.setOperation(opMap);
        opMap.setOperation(operations);

        operations.add(getCo("/"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "");
        Assert.assertEquals(operations.get(0).path, "");
        Assert.assertFalse(operations.get(0).subresourceOperation);
        operations.clear();

        operations.add(getCo("/test"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "/test");
        Assert.assertEquals(operations.get(0).path, "");
        Assert.assertFalse(operations.get(0).subresourceOperation);
        operations.clear();

        operations.add(getCo("/"));
        operations.add(getCo("/op1"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "");
        Assert.assertEquals(operations.get(0).path, "/");
        Assert.assertFalse(operations.get(0).subresourceOperation);
        Assert.assertEquals(operations.get(1).path, "/op1");
        Assert.assertTrue(operations.get(1).subresourceOperation);
        operations.clear();

        operations.add(getCo("/group1/subgroup1/op1"));
        operations.add(getCo("/group1/subgroup1/op2"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "/group1/subgroup1");
        Assert.assertEquals(operations.get(0).path, "/op1");
        Assert.assertTrue(operations.get(0).subresourceOperation);
        Assert.assertEquals(operations.get(1).path, "/op2");
        Assert.assertTrue(operations.get(1).subresourceOperation);
        operations.clear();

        operations.add(getCo("/op1"));
        operations.add(getCo("/op2"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "");
        Assert.assertEquals(operations.get(0).path, "/op1");
        Assert.assertTrue(operations.get(0).subresourceOperation);
        Assert.assertEquals(operations.get(1).path, "/op2");
        Assert.assertTrue(operations.get(1).subresourceOperation);
        operations.clear();

        operations.add(getCo("/group1"));
        operations.add(getCo("/group1/op1"));
        codegen.postProcessOperationsWithModels(objs, Collections.emptyList());
        Assert.assertEquals(objs.get("commonPath"), "/group1");
        Assert.assertEquals(operations.get(0).path, "");
        Assert.assertFalse(operations.get(0).subresourceOperation);
        Assert.assertEquals(operations.get(1).path, "/op1");
        Assert.assertTrue(operations.get(1).subresourceOperation);
        operations.clear();
    }

    private CodegenOperation getCo(final String path) {
        final CodegenOperation co = new CodegenOperation();
        co.path = path;
        return co;
    }
}
