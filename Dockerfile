FROM registry.access.redhat.com/ubi9-minimal:9.2 AS ubi
# -- Stage scratch
FROM registry.access.redhat.com/ubi9-micro:9.2 AS scratch
# -- Final Stage
FROM scratch
COPY --from=ubi /usr/lib64/libgcc_s.so.1 /usr/lib64/libgcc_s.so.1
COPY --from=ubi /usr/lib64/libstdc++.so.6 /usr/lib64/libstdc++.so.6
COPY --from=ubi /usr/lib64/libz.so.1 /usr/lib64/libz.so.1

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root target/rinhaBackend /work/application

USER 1001

CMD ["./application"]