docker run --name oraman --rm \
    -p 8080:8080 \
    -e "ORAMAN_DATASOURCE_URL=jdbc:oracle:thin:@172.19.65.60:1522:motion12c" \
    -e "ORAMAN_DATASOURCE_USERNAME=system" \
    -e "ORAMAN_DATASOURCE_PASSWORD=*******" \
    oraman
