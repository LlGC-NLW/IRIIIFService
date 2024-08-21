export JAVA_HOME=/etc/alternatives/java_sdk_17
sudo podman stop iiif_redis_migration ; sudo podman rm iiif_redis_migration; sh ../../redis/run
mvn clean package  -DskipTests && cd .. && sudo buildah bud -t iiif_iriiif && sudo podman stop iiif_iriiif_migration ;  sudo podman rm  iiif_iriiif_migration ; sh run && cd IRIIIFService && sudo podman logs -f  iiif_iriiif_migration
