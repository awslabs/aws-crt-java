/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.util.List;
import java.util.Collections;

public class ChecksumConfig {

    public enum ChecksumLocation {

        NONE(0),
        HEADER(1),
        TRAILER(2);

        ChecksumLocation(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() {
            return nativeValue;
        }

        private int nativeValue;
    };

    private ChecksumLocation location = ChecksumLocation.NONE;
    private ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.NONE;
    private boolean validateChecksum = false;
    private List<ChecksumAlgorithm> validateChecksumAlgorithmList = null;

    public ChecksumConfig() {
    }

    /**
     * If NONE. No request payload checksum will be add and calculated.
     *
     * If HEADER, the checksum will be calculated by client and added related header
     * to the request sent.
     *
     * If TRAILER, the payload will be aws_chunked encoded, The checksum will be
     * calculated while reading the
     * payload by client. Related header will be added to the trailer part of the
     * encoded payload. Note the payload of
     * the original request cannot be aws-chunked encoded already. Otherwise, error
     * will be raised.
     *
     * @param location The location of client added request payload checksum header.
     * @return this
     */
    public ChecksumConfig withChecksumLocation(ChecksumLocation location) {
        this.location = location;
        return this;
    }

    /**
     * @return The location of client added checksum header.
     */
    public ChecksumLocation getChecksumLocation() {
        return this.location;
    }

    /**
     * The checksum algorithm used to calculate the checksum of payload uploaded.
     * Must be set if location is not AWS_SCL_NONE. Must be AWS_SCA_NONE if location
     * is AWS_SCL_NONE.
     *
     * @param algorithm The checksum algorithm used to calculate the checksum of
     *                  payload uploaded.
     * @return this
     */
    public ChecksumConfig withChecksumAlgorithm(ChecksumAlgorithm algorithm) {
        this.checksumAlgorithm = algorithm;
        return this;
    }

    /**
     * @return The checksum algorithm used to calculate the checksum of payload
     *         uploaded.
     */
    public ChecksumAlgorithm getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    /**
     * Enable checksum mode header will be attached to get requests, this will tell
     * s3 to send back checksums headers if they exist.
     *
     * For object has checksum for the whole object, the checksum of whole object
     * will be calculated and validated. The result will finish with a did validate
     * field.
     * For object has checksum for parts, if ALL the parts have been validated, the
     * result will finish with a did validate field. If any part failed the
     * validation, AWS_ERROR_S3_RESPONSE_CHECKSUM_MISMATCH will be raised.
     *
     * @param validateChecksum Validate the checksum of response if server provides.
     * @return this
     */
    public ChecksumConfig withValidateChecksum(boolean validateChecksum) {
        this.validateChecksum = validateChecksum;
        return this;
    }

    public boolean getValidateChecksum() {
        return validateChecksum;
    }

    /**
     * Ignored when validate_response_checksum is not set.
     * If not set all the algorithms will be selected as default behavior.
     *
     * The list of algorithms for user to pick up when validate the checksum. Client
     * will pick up the algorithm from the list with the priority based on
     * performance, and the algorithm sent by server. The priority based on
     * performance is [CRC32C, CRC32, SHA1, SHA256].
     *
     * If the response checksum was validated by client, the result will indicate
     * which algorithm was picked.
     *
     * @param validateChecksumAlgorithmList The list of algorithm picked to validate
     *                                      checksum from response.
     * @return this
     */
    public ChecksumConfig withValidateChecksumAlgorithmList(List<ChecksumAlgorithm> validateChecksumAlgorithmList) {
        this.validateChecksumAlgorithmList = validateChecksumAlgorithmList != null
                ? Collections.unmodifiableList(validateChecksumAlgorithmList)
                : null;
        return this;
    }

    /**
     * @return The list of algorithm picked to validate checksum from response.
     */
    public List<ChecksumAlgorithm> getValidateChecksumAlgorithmList() {
        return this.validateChecksumAlgorithmList;
    }
}
