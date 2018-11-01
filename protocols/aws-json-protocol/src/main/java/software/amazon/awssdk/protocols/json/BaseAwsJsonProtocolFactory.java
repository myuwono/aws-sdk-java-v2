/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.protocols.json;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import software.amazon.awssdk.annotations.SdkProtectedApi;
import software.amazon.awssdk.annotations.SdkTestInternalApi;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.Request;
import software.amazon.awssdk.core.SdkPojo;
import software.amazon.awssdk.core.http.HttpResponseHandler;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.protocols.core.OperationInfo;
import software.amazon.awssdk.protocols.core.ProtocolMarshaller;
import software.amazon.awssdk.protocols.json.internal.AwsStructuredPlainJsonFactory;
import software.amazon.awssdk.protocols.json.internal.marshall.JsonProtocolMarshallerBuilder;
import software.amazon.awssdk.protocols.json.internal.unmarshall.AwsJsonErrorMessageParser;
import software.amazon.awssdk.protocols.json.internal.unmarshall.AwsJsonProtocolErrorUnmarshaller;
import software.amazon.awssdk.protocols.json.internal.unmarshall.AwsJsonResponseHandler;
import software.amazon.awssdk.protocols.json.internal.unmarshall.JsonProtocolUnmarshaller;
import software.amazon.awssdk.protocols.json.internal.unmarshall.JsonResponseHandler;

@SdkProtectedApi
public abstract class BaseAwsJsonProtocolFactory {
    /**
     * Content type resolver implementation for plain text AWS_JSON services.
     */
    protected static final JsonContentTypeResolver AWS_JSON = new DefaultJsonContentTypeResolver("application/x-amz-json-");
    protected final AwsJsonProtocolMetadata protocolMetadata;
    private final Map<String, Supplier<SdkPojo>> modeledExceptions;
    private final Supplier<SdkPojo> defaultServiceExceptionSupplier;
    private final String customErrorCodeFieldName;

    protected BaseAwsJsonProtocolFactory(Builder<?> builder) {
        this.protocolMetadata = builder.protocolMetadata.build();
        this.modeledExceptions = unmodifiableMap(new HashMap<>(builder.modeledExceptions));
        this.defaultServiceExceptionSupplier = builder.defaultServiceExceptionSupplier;
        this.customErrorCodeFieldName = builder.customErrorCodeFieldName;
    }

    /**
     * Creates a new response handler with the given {@link JsonOperationMetadata} and a supplier of the POJO response
     * type.
     *
     * @param operationMetadata Metadata about operation being unmarshalled.
     * @param pojoSupplier {@link Supplier} of the POJO response type.
     * @param <T> Type being unmarshalled.
     * @return HttpResponseHandler that will handle the HTTP response and unmarshall into a POJO.
     */
    public <T extends SdkPojo> HttpResponseHandler<T> createResponseHandler(JsonOperationMetadata operationMetadata,
                                                                            Supplier<SdkPojo> pojoSupplier) {
        return createResponseHandler(operationMetadata, r -> pojoSupplier.get());
    }

    /**
     * Creates a new response handler with the given {@link JsonOperationMetadata} and a supplier of the POJO response
     * type.
     *
     * @param operationMetadata Metadata about operation being unmarshalled.
     * @param pojoSupplier {@link Supplier} of the POJO response type. Has access to the HTTP response, primarily for polymorphic
     * deserialization as seen in event stream (i.e. unmarshalled event depends on ':event-type' header).
     * @param <T> Type being unmarshalled.
     * @return HttpResponseHandler that will handle the HTTP response and unmarshall into a POJO.
     */
    public <T extends SdkPojo> HttpResponseHandler<T> createResponseHandler(JsonOperationMetadata operationMetadata,
                                                                            Function<SdkHttpFullResponse, SdkPojo> pojoSupplier) {
        JsonProtocolUnmarshaller<T> unmarshaller = createJsonProtocolUnmarshaller();
        return new AwsJsonResponseHandler<>(
            new JsonResponseHandler<>(unmarshaller,
                                      pojoSupplier,
                                      operationMetadata.hasStreamingSuccessResponse(),
                                      operationMetadata.isPayloadJson()));
    }

    private <T extends SdkPojo> JsonProtocolUnmarshaller<T> createJsonProtocolUnmarshaller() {
        return new JsonProtocolUnmarshaller<>(getSdkFactory().getJsonFactory());
    }

    /**
     * Creates a response handler for handling a error response (non 2xx response).
     */
    public HttpResponseHandler<AwsServiceException> createErrorResponseHandler(JsonOperationMetadata errorResponseMetadata) {
        return AwsJsonProtocolErrorUnmarshaller
            .builder()
            .jsonProtocolUnmarshaller(createJsonProtocolUnmarshaller())
            .exceptions(modeledExceptions)
            .errorCodeParser(getSdkFactory().getErrorCodeParser(customErrorCodeFieldName))
            .errorMessageParser(AwsJsonErrorMessageParser.DEFAULT_ERROR_MESSAGE_PARSER)
            .jsonFactory(getSdkFactory().getJsonFactory())
            .defaultExceptionSupplier(defaultServiceExceptionSupplier)
            .build();
    }

    private StructuredJsonGenerator createGenerator(OperationInfo operationInfo) {
        if (operationInfo.hasPayloadMembers() || protocolMetadata.protocol() == AwsJsonProtocol.AWS_JSON) {
            return createGenerator();
        } else {
            return StructuredJsonGenerator.NO_OP;
        }
    }

    @SdkTestInternalApi
    StructuredJsonGenerator createGenerator() {
        return getSdkFactory().createWriter(getContentType());
    }

    @SdkTestInternalApi
    protected String getContentType() {
        return getContentTypeResolver().resolveContentType(protocolMetadata);
    }

    /**
     * @return Content type resolver implementation to use.
     */
    protected JsonContentTypeResolver getContentTypeResolver() {
        return AWS_JSON;
    }

    /**
     * @return Instance of {@link StructuredJsonFactory} to use in creating handlers.
     */
    protected BaseAwsStructuredJsonFactory getSdkFactory() {
        return AwsStructuredPlainJsonFactory.SDK_JSON_FACTORY;
    }

    public <T> ProtocolMarshaller<Request<T>> createProtocolMarshaller(
        OperationInfo operationInfo, T origRequest) {
        return JsonProtocolMarshallerBuilder.<T>standard()
            .jsonGenerator(createGenerator(operationInfo))
            .contentType(getContentType())
            .operationInfo(operationInfo)
            .originalRequest(origRequest)
            .sendExplicitNullForPayload(false)
            .build();
    }

    /**
     * Builder for {@link AwsJsonProtocolFactory}.
     */
    public abstract static class Builder<SubclassT extends Builder> {

        private final AwsJsonProtocolMetadata.Builder protocolMetadata = AwsJsonProtocolMetadata.builder();
        private final Map<String, Supplier<SdkPojo>> modeledExceptions = new HashMap<>();
        private Supplier<SdkPojo> defaultServiceExceptionSupplier;
        private String customErrorCodeFieldName;

        protected Builder() {
        }

        public SubclassT registerModeledException(String errorCode, Supplier<SdkPojo> exceptionBuilderSupplier) {
            modeledExceptions.put(errorCode, exceptionBuilderSupplier);
            return getSubclass();
        }

        public SubclassT defaultServiceExceptionSupplier(Supplier<SdkPojo> exceptionBuilderSupplier) {
            this.defaultServiceExceptionSupplier = exceptionBuilderSupplier;
            return getSubclass();
        }

        public SubclassT protocol(AwsJsonProtocol protocol) {
            protocolMetadata.protocol(protocol);
            return getSubclass();
        }

        public SubclassT protocolVersion(String protocolVersion) {
            protocolMetadata.protocolVersion(protocolVersion);
            return getSubclass();
        }

        public SubclassT customErrorCodeFieldName(String customErrorCodeFieldName) {
            this.customErrorCodeFieldName = customErrorCodeFieldName;
            return getSubclass();
        }

        @SuppressWarnings("unchecked")
        private SubclassT getSubclass() {
            return (SubclassT) this;
        }

    }
}
