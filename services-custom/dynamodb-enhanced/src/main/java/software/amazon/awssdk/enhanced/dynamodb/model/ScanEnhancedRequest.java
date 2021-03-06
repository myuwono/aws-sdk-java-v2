/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.awssdk.enhanced.dynamodb.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Defines parameters used to when scanning a DynamoDb table or index using the scan() operation (such as
 * {@link DynamoDbTable#scan(ScanEnhancedRequest)}).
 * <p>
 * All parameters are optional.
 */
@SdkPublicApi
public final class ScanEnhancedRequest {

    private final Map<String, AttributeValue> exclusiveStartKey;
    private final Integer limit;
    private final Boolean consistentRead;
    private final Expression filterExpression;
    private final List<String> attributesToProject;

    private ScanEnhancedRequest(Builder builder) {
        this.exclusiveStartKey = builder.exclusiveStartKey;
        this.limit = builder.limit;
        this.consistentRead = builder.consistentRead;
        this.filterExpression = builder.filterExpression;
        this.attributesToProject = builder.attributesToProject != null
                ? Collections.unmodifiableList(builder.attributesToProject)
                : null;
    }

    /**
     * Creates a newly initialized builder for a request object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with all existing values on the request object.
     */
    public Builder toBuilder() {
        return builder().exclusiveStartKey(exclusiveStartKey)
                        .limit(limit)
                        .consistentRead(consistentRead)
                        .filterExpression(filterExpression)
                        .attributesToProject(attributesToProject);
    }

    /**
     * Returns the value of the exclusive start key set on this request object, or null if it doesn't exist.
     */
    public Map<String, AttributeValue> exclusiveStartKey() {
        return exclusiveStartKey;
    }

    /**
     * Returns the value of limit set on this request object, or null if it doesn't exist.
     */
    public Integer limit() {
        return limit;
    }

    /**
     * Returns the value of consistent read, or false if it has not been set.
     */
    public Boolean consistentRead() {
        return consistentRead;
    }

    /**
     * Returns the return result filter {@link Expression} set on this request object, or null if it doesn't exist.
     */
    public Expression filterExpression() {
        return filterExpression;
    }

    /**
     * Returns the list of projected attributes on this request object, or null if no projection is specified.
     */
    public List<String> attributesToProject() {
        return attributesToProject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScanEnhancedRequest scan = (ScanEnhancedRequest) o;

        if (exclusiveStartKey != null ? ! exclusiveStartKey.equals(scan.exclusiveStartKey) :
            scan.exclusiveStartKey != null) {
            return false;
        }
        if (limit != null ? ! limit.equals(scan.limit) : scan.limit != null) {
            return false;
        }
        if (consistentRead != null ? ! consistentRead.equals(scan.consistentRead) : scan.consistentRead != null) {
            return false;
        }
        if (attributesToProject != null
                ? ! attributesToProject.equals(scan.attributesToProject)
                : scan.attributesToProject != null
        ) {
            return false;
        }
        return filterExpression != null ? filterExpression.equals(scan.filterExpression) : scan.filterExpression == null;
    }

    @Override
    public int hashCode() {
        int result = exclusiveStartKey != null ? exclusiveStartKey.hashCode() : 0;
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (consistentRead != null ? consistentRead.hashCode() : 0);
        result = 31 * result + (filterExpression != null ? filterExpression.hashCode() : 0);
        result = 31 * result + (attributesToProject != null ? attributesToProject.hashCode() : 0);
        return result;
    }

    /**
     * A builder that is used to create a request with the desired parameters.
     */
    public static final class Builder {
        private Map<String, AttributeValue> exclusiveStartKey;
        private Integer limit;
        private Boolean consistentRead;
        private Expression filterExpression;
        private List<String> attributesToProject;

        private Builder() {
        }

        /**
         * The primary key of the first item that this operation will evaluate. By default, the operation will evaluate
         * the whole dataset. If used, normally this parameter is populated with the value that was returned for
         * {@link Page#lastEvaluatedKey()} in the previous operation.
         *
         * @param exclusiveStartKey the primary key value where DynamoDb should start to evaluate items
         * @return a builder of this type
         */
        public Builder exclusiveStartKey(Map<String, AttributeValue> exclusiveStartKey) {
            this.exclusiveStartKey = exclusiveStartKey != null ? new HashMap<>(exclusiveStartKey) : null;
            return this;
        }

        /**
         * Sets a limit on how many items to evaluate in the scan. If not set, the operation uses
         * the maximum values allowed.
         * <p>
         * <b>Note:</b>The limit does not refer to the number of items to return, but how many items
         * the database should evaluate while executing the scan. Use limit together with {@link Page#lastEvaluatedKey()}
         * and {@link #exclusiveStartKey} in subsequent scan calls to evaluate <em>limit</em> items per call.
         *
         * @param limit the maximum number of items to evalute
         * @return a builder of this type
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Determines the read consistency model: If set to true, the operation uses strongly consistent reads; otherwise,
         * the operation uses eventually consistent reads.
         * <p>
         * By default, the value of this property is set to <em>false</em>.
         *
         * @param consistentRead sets consistency model of the operation to use strong consistency if true
         * @return a builder of this type
         */
        public Builder consistentRead(Boolean consistentRead) {
            this.consistentRead = consistentRead;
            return this;
        }

        /**
         * Refines the scan results by applying the filter expression on the results returned
         * from the scan and discards items that do not match. See {@link Expression} for examples
         * and constraints.
         * <p>
         * <b>Note:</b> Using the filter expression does not reduce the cost of the scan, since it is applied
         * <em>after</em> the database has found matching items.
         *
         * @param filterExpression an expression that filters results of evaluating the scan
         * @return a builder of this type
         */
        public Builder filterExpression(Expression filterExpression) {
            this.filterExpression = filterExpression;
            return this;
        }

        /**
         * <p>
         * Sets a collection of the attribute names to be retrieved from the database. These attributes can include
         * scalars, sets, or elements of a JSON document.
         * </p>
         * <p>
         * If no attribute names are specified, then all attributes will be returned. If any of the requested attributes
         * are not found, they will not appear in the result.
         * </p>
         * <p>
         * For more information, see <a href=
         * "https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.AccessingItemAttributes.html"
         * >Accessing Item Attributes</a> in the <i>Amazon DynamoDB Developer Guide</i>.
         * </p>
         * @param attributesToProject
         *        A collection of the attributes names to be retrieved from the database.
         * @return Returns a reference to this object so that method calls can be chained together.
         */
        public Builder attributesToProject(Collection<String> attributesToProject) {
            this.attributesToProject = attributesToProject != null ? new ArrayList<>(attributesToProject) : null;
            return this;
        }

        /**
         * <p>
         * Sets one or more attribute names to be retrieved from the database. These attributes can include
         * scalars, sets, or elements of a JSON document.
         * </p>
         * <p>
         * If no attribute names are specified, then all attributes will be returned. If any of the requested attributes
         * are not found, they will not appear in the result.
         * </p>
         * <p>
         * For more information, see <a href=
         * "https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.AccessingItemAttributes.html"
         * >Accessing Item Attributes</a> in the <i>Amazon DynamoDB Developer Guide</i>.
         * </p>
         * @param attributesToProject
         *        One or more  attributes names to be retrieved from the database.
         * @return Returns a reference to this object so that method calls can be chained together.
         */
        public Builder attributesToProject(String... attributesToProject) {
            return attributesToProject(Arrays.asList(attributesToProject));
        }

        /**
         * <p>
         * Adds a single attribute name to be retrieved from the database. This attribute can include
         * scalars, sets, or elements of a JSON document.
         * </p>
         * <p>
         * For more information, see <a href=
         * "https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.AccessingItemAttributes.html"
         * >Accessing Item Attributes</a> in the <i>Amazon DynamoDB Developer Guide</i>.
         * </p>
         * @param attributeToProject
         *        An additional single attribute name to be retrieved from the database.
         * @return Returns a reference to this object so that method calls can be chained together.
         */
        public Builder addAttributeToProject(String attributeToProject) {
            if (attributesToProject == null) {
                attributesToProject = new ArrayList<>();
            }
            attributesToProject.add(attributeToProject);
            return this;
        }

        public ScanEnhancedRequest build() {
            return new ScanEnhancedRequest(this);
        }
    }
}
