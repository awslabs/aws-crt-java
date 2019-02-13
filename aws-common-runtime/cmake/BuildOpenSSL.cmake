
if($CMAKE_SIZEOF_VOID_P == 4)
    set(SETARCH_CMD "setarch i386")
    set(OPENSSL_C_FLAGS "-m32")
else()
    set(SETARCH_CMD "")
    set(OPENSSL_C_FLAGS "")
endif()

if("${TARGET_ARCH}" STREQUAL ANDROID)
    ExternalProject_Add(S2N
            PREFIX ${AWS_DEPS_BUILD_DIR}
            URL ${OPENSSL_URL}
            BUILD_IN_SOURCE 0
            UPDATE_DISCONNECTED 1
            CONFIGURE_COMMAND "${SETARCH_CMD} ./config -fPIC  \
                ${OPENSSL_C_FLAGS} no-md2 no-rc5 no-rfc3779 no-sctp no-ssl-trace no-zlib     \
                no-hw no-mdc2 no-seed no-idea no-camellia\
                no-bf no-ripemd no-dsa no-ssl2 no-ssl3 no-capieng     \
                -DSSL_FORBID_ENULL -DOPENSSL_NO_DTLS1 -DOPENSSL_NO_HEARTBEATS   \
                --prefix=\"${AWS_DEPS_BUILD_DIR}/openssl\""
            BUILD_COMMAND "make -j 12"
            INSTALL_COMMAND "make install_sw"
            )
else()
    ExternalProject_Add(S2N
            PREFIX ${AWS_DEPS_BUILD_DIR}
            URL ${OPENSSL_URL}
            BUILD_IN_SOURCE 0
            UPDATE_DISCONNECTED 1
            CONFIGURE_COMMAND "${SETARCH_CMD} ./config -fPIC  \
                ${OPENSSL_C_FLAGS} no-md2 no-rc5 no-rfc3779 no-sctp no-ssl-trace no-zlib     \
                no-hw no-mdc2 no-seed no-idea no-camellia\
                no-bf no-ripemd no-dsa no-ssl2 no-ssl3 no-capieng     \
                -DSSL_FORBID_ENULL -DOPENSSL_NO_DTLS1 -DOPENSSL_NO_HEARTBEATS   \
                --prefix=\"${AWS_DEPS_BUILD_DIR}/openssl\""
            BUILD_COMMAND "make -j 12"
            INSTALL_COMMAND "make install_sw"
            )
endif()
