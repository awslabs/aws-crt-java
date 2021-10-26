package com.amazonaws.s3;

import com.amazonaws.s3.model.*;
import com.amazonaws.s3.model.Object;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class ListObjectsV2OutputParserHandler extends DefaultHandler {

    enum State {
        ParsingRoot,
        ParsingContentsList,
        ParsingContentsOwner,
        ParsingCommonPrefixesList
    }

    private static final String LIST_BUCKET_RESULT_TAG = "ListBucketResult";
    private static final String LIST_BUCKET_RESULT_IS_TRUNCATED_TAG = "IsTruncated";
    private static final String LIST_BUCKET_RESULT_CONTENTS_TAG = "Contents";
    private static final String LIST_BUCKET_RESULT_CONTENTS_ETAG_TAG = "ETag";
    private static final String LIST_BUCKET_RESULT_CONTENTS_KEY_TAG = "Key";
    private static final String LIST_BUCKET_RESULT_CONTENTS_LAST_MODIFIED_TAG = "LastModified";
    private static final String LIST_BUCKET_RESULT_CONTENTS_OWNER_TAG = "Owner";
    private static final String LIST_BUCKET_RESULT_CONTENTS_OWNER_DISPLAY_NAME_TAG = "DisplayName";
    private static final String LIST_BUCKET_RESULT_CONTENTS_OWNER_ID_TAG = "ID";
    private static final String LIST_BUCKET_RESULT_CONTENTS_SIZE_TAG = "Size";
    private static final String LIST_BUCKET_RESULT_CONTENTS_STORAGE_CLASS_TAG = "StorageClass";
    private static final String LIST_BUCKET_RESULT_NAME_TAG = "Name";
    private static final String LIST_BUCKET_RESULT_PREFIX_TAG = "Prefix";
    private static final String LIST_BUCKET_RESULT_DELIMITER_TAG = "Delimiter";
    private static final String LIST_BUCKET_RESULT_MAX_KEYS_TAG = "MaxKeys";
    private static final String LIST_BUCKET_RESULT_COMMON_PREFIXES_TAG = "CommonPrefixes";
    private static final String LIST_BUCKET_RESULT_COMMON_PREFIXES_PREFIX_TAG = "Prefix";
    private static final String LIST_BUCKET_RESULT_ENCODING_TYPE_TAG = "EncodingType";
    private static final String LIST_BUCKET_RESULT_KEY_COUNT_TAG = "KeyCount";
    private static final String LIST_BUCKET_RESULT_CONTINUATION_TOKEN_TAG = "ContinuationToken";
    private static final String LIST_BUCKET_RESULT_NEXT_CONTINUATION_TOKEN_TAG = "NextContinuationToken";
    private static final String LIST_BUCKET_RESULT_START_AFTER_TAG = "StartAfter";

    private StringBuilder currentElementValue = new StringBuilder();
    private State state = State.ParsingRoot;
    private ListObjectsV2Output.Builder resultBuilder;
    private List<Object> contents;
    private Object.Builder objectBuilder;
    private Owner.Builder ownerBuilder;
    private CommonPrefix.Builder commonPrefixBuilder;
    private List<CommonPrefix> commonPrefixes;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        currentElementValue = new StringBuilder();

        switch (state) {
            case ParsingRoot:
                startRootElement(qName);
                break;

            case ParsingContentsList:
                startContentsListElement(qName);
                break;

            case ParsingContentsOwner:
                startContentsOwnerElement(qName);
                break;

            case ParsingCommonPrefixesList:
                startCommonPrefixesListElement(qName);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        switch (state) {
            case ParsingRoot:
                endRootElement(qName);
                break;

            case ParsingContentsList:
                endContentsListElement(qName);
                break;

            case ParsingContentsOwner:
                endContentsOwnerElement(qName);
                break;

            case ParsingCommonPrefixesList:
                endCommonPrefixesListElement(qName);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        currentElementValue.append(ch, start, length);
    }

    private void startRootElement(final String qName) {
        switch (qName) {
            case LIST_BUCKET_RESULT_TAG:
                resultBuilder = ListObjectsV2Output.builder();
                contents = new ArrayList<>();
                resultBuilder.contents(contents);
                break;

            case LIST_BUCKET_RESULT_CONTENTS_TAG:
                objectBuilder = Object.builder();
                state = State.ParsingContentsList;
                break;

            case LIST_BUCKET_RESULT_COMMON_PREFIXES_TAG:
                commonPrefixes = new ArrayList<>();
                state = State.ParsingCommonPrefixesList;
                break;
        }
    }

    private void startContentsListElement(final String qName) {
        switch (qName) {
            case LIST_BUCKET_RESULT_CONTENTS_OWNER_TAG:
                ownerBuilder = Owner.builder();
                state = State.ParsingContentsOwner;
                break;
        }
    }

    private void startContentsOwnerElement(final String qName) {
        // all tags have string content. Nothing to allocate.
    }

    private void startCommonPrefixesListElement(final String qName) {

        switch (qName) {
            case LIST_BUCKET_RESULT_COMMON_PREFIXES_PREFIX_TAG:
                commonPrefixBuilder = CommonPrefix.builder();
                break;
        }
    }

    private void endRootElement(final String qName) {
        switch (qName) {
            case LIST_BUCKET_RESULT_TAG:
                // done. resultBuilder is complete.
                break;

            case LIST_BUCKET_RESULT_IS_TRUNCATED_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.isTruncated(Boolean.parseBoolean(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_NAME_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.name(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_PREFIX_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.prefix(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_DELIMITER_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.delimiter(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_MAX_KEYS_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.maxKeys(Integer.parseInt(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_ENCODING_TYPE_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.encodingType(EncodingType.fromValue(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_KEY_COUNT_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.keyCount(Integer.parseInt(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_CONTINUATION_TOKEN_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.continuationToken(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_NEXT_CONTINUATION_TOKEN_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.nextContinuationToken(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_START_AFTER_TAG:
                if (currentElementValue.length() > 0) {
                    resultBuilder.startAfter(currentElementValue.toString());
                }
                break;
        }
    }

    private void endContentsListElement(final String qName) {
        switch (qName) {
            case LIST_BUCKET_RESULT_CONTENTS_ETAG_TAG:
                if (currentElementValue.length() > 0) {
                    objectBuilder.eTag(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_KEY_TAG:
                if (currentElementValue.length() > 0) {
                    objectBuilder.key(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_LAST_MODIFIED_TAG:
                if (currentElementValue.length() > 0) {
                    objectBuilder.lastModified(Instant.parse(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_OWNER_TAG:
                objectBuilder.owner(ownerBuilder.build());
                break;

            case LIST_BUCKET_RESULT_CONTENTS_SIZE_TAG:
                if (currentElementValue.length() > 0) {
                    // TODO: remove cast to int when model is fixed to be long instead of integer
                    objectBuilder.size((int) Long.parseLong(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_STORAGE_CLASS_TAG:
                if (currentElementValue.length() > 0) {
                    objectBuilder.storageClass(ObjectStorageClass.fromValue(currentElementValue.toString()));
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_TAG:
                contents.add(objectBuilder.build());
                state = State.ParsingRoot;
                break;
        }
    }

    private void endContentsOwnerElement(final String qName) {
        switch (qName) {

            case LIST_BUCKET_RESULT_CONTENTS_OWNER_DISPLAY_NAME_TAG:
                if (currentElementValue.length() > 0) {
                    ownerBuilder.displayName(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_OWNER_ID_TAG:
                if (currentElementValue.length() > 0) {
                    ownerBuilder.iD(currentElementValue.toString());
                }
                break;

            case LIST_BUCKET_RESULT_CONTENTS_OWNER_TAG:
                objectBuilder.owner(ownerBuilder.build());
                state = State.ParsingContentsList;
                break;
        }
    }

    private void endCommonPrefixesListElement(final String qName) {

        switch (qName) {
            case LIST_BUCKET_RESULT_COMMON_PREFIXES_PREFIX_TAG:
                if (currentElementValue.length() > 0) {
                    commonPrefixBuilder.prefix(currentElementValue.toString());
                    commonPrefixes.add(commonPrefixBuilder.build());
                }
                break;

            case LIST_BUCKET_RESULT_COMMON_PREFIXES_TAG:
                resultBuilder.commonPrefixes(commonPrefixes);
                state = State.ParsingRoot;
                break;
        }
    }

    public ListObjectsV2Output getOutput() {
        return resultBuilder.build();
    }
}
