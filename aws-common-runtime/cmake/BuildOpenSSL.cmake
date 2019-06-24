
if(${CMAKE_SIZEOF_VOID_P} STREQUAL 4)
    set(OPENSSL_C_FLAGS "-m32")
else()
    set(OPENSSL_C_FLAGS "")
endif()

if("${TARGET_ARCH}" STREQUAL ANDROID)
    ExternalProject_Add(OpenSSL
        PREFIX ${AWS_DEPS_BUILD_DIR}
        GIT_REPOSITORY ${OPENSSL_URL}
        GIT_TAG ${OPENSSL_SHA}
        BUILD_IN_SOURCE 1
        UPDATE_COMMAND ""
        CONFIGURE_COMMAND ./config -fPIC ${OPENSSL_C_FLAGS} 
            no-md2 no-rc5 no-rfc3779 no-sctp no-ssl-trace no-zlib no-hw no-mdc2 
            no-seed no-idea no-camellia no-bf no-dsa no-ssl3 no-capieng 
            no-unit-test no-tests
            -DSSL_FORBID_ENULL -DOPENSSL_NO_DTLS1 -DOPENSSL_NO_HEARTBEATS
            --prefix=${AWS_DEPS_INSTALL_DIR}
            --openssldir=${AWS_DEPS_INSTALL_DIR}
        BUILD_COMMAND make -j 12
        INSTALL_COMMAND make install_sw
        )
else()
    ExternalProject_Add(OpenSSL
        PREFIX ${AWS_DEPS_BUILD_DIR}
        GIT_REPOSITORY ${OPENSSL_URL}
        GIT_TAG ${OPENSSL_SHA}
        BUILD_IN_SOURCE 1
        UPDATE_COMMAND ""
        CONFIGURE_COMMAND ./config -fPIC ${OPENSSL_C_FLAGS} 
            no-md2 no-rc5 no-rfc3779 no-sctp no-ssl-trace no-zlib no-hw no-mdc2 
            no-seed no-idea no-camellia no-bf no-dsa no-ssl3 no-capieng 
            no-unit-test no-tests
            -DSSL_FORBID_ENULL -DOPENSSL_NO_DTLS1 -DOPENSSL_NO_HEARTBEATS 
            --prefix=${AWS_DEPS_INSTALL_DIR} 
            --openssldir=${AWS_DEPS_INSTALL_DIR}
        BUILD_COMMAND make -j 12
        INSTALL_COMMAND make install_sw
        )
endif()
