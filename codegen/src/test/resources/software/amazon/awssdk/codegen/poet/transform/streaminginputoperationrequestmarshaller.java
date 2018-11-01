package software.amazon.awssdk.services.jsonprotocoltests.transform;

import software.amazon.awssdk.annotations.Generated;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.Request;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.http.HttpMethodName;
import software.amazon.awssdk.core.runtime.transform.Marshaller;
import software.amazon.awssdk.protocols.core.OperationInfo;
import software.amazon.awssdk.protocols.core.ProtocolMarshaller;
import software.amazon.awssdk.protocols.json.BaseAwsJsonProtocolFactory;
import software.amazon.awssdk.services.jsonprotocoltests.model.StreamingInputOperationRequest;
import software.amazon.awssdk.utils.Validate;

/**
 * {@link StreamingInputOperationRequest} Marshaller
 */
@Generated("software.amazon.awssdk:codegen")
@SdkInternalApi
public class StreamingInputOperationRequestMarshaller implements
                                                      Marshaller<Request<StreamingInputOperationRequest>, StreamingInputOperationRequest> {
    private static final OperationInfo SDK_OPERATION_BINDING = OperationInfo.builder()
                                                                            .requestUri("/2016-03-11/streamingInputOperation").httpMethodName(HttpMethodName.POST).hasExplicitPayloadMember(true)
                                                                            .hasPayloadMembers(true).serviceName("JsonProtocolTests").hasStreamingInput(true).build();

    private final BaseAwsJsonProtocolFactory protocolFactory;

    public StreamingInputOperationRequestMarshaller(BaseAwsJsonProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    public Request<StreamingInputOperationRequest> marshall(StreamingInputOperationRequest streamingInputOperationRequest) {
        Validate.paramNotNull(streamingInputOperationRequest, "streamingInputOperationRequest");
        try {
            ProtocolMarshaller<Request<StreamingInputOperationRequest>> protocolMarshaller = protocolFactory
                .createProtocolMarshaller(SDK_OPERATION_BINDING, streamingInputOperationRequest);
            return protocolMarshaller.marshall(streamingInputOperationRequest);
        } catch (Exception e) {
            throw SdkClientException.builder().message("Unable to marshall request to JSON: " + e.getMessage()).cause(e).build();
        }
    }
}

