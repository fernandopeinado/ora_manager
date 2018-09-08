package br.com.cas10.oraman;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "oraman", ignoreUnknownFields = true)
@Validated
public class OramanProperties {

  @NotNull
  private String home;
  @Valid
  private DataSource dataSource = new DataSource();
  @Valid
  private Archive archive = new Archive();

  public String getHome() {
    return home;
  }

  public void setHome(String home) {
    this.home = home;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Archive getArchive() {
    return archive;
  }

  public void setArchive(Archive archive) {
    this.archive = archive;
  }

  public static class Archive {

    @NotNull
    private String dir;

    public String getDir() {
      return dir;
    }

    public void setDir(String dir) {
      this.dir = dir;
    }
  }

  public static class DataSource {

    @NotNull
    private String url;
    @NotNull
    private String username;
    @NotNull
    private String password;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
