package br.com.zaiac.ebcofilemgmt.model;

public class SftpCredentials {

  private Integer sitePriority;
  private String siteSFTPDestination;
  private Integer siteSFTPPort;
  private String siteSFTPUsername;
  private String siteSFTPPassword;

  public Integer getSitePriority() {
    return sitePriority;
  }

  public String getSiteSFTPDestination() {
    return siteSFTPDestination;
  }

  public Integer getSiteSFTPPort() {
    return siteSFTPPort;
  }

  public String getSiteSFTPUsername() {
    return siteSFTPUsername;
  }

  public String getSiteSFTPPassword() {
    return siteSFTPPassword;
  }

  public SftpCredentials(
    Integer sitePriority,
    String siteSFTPDestination,
    Integer siteSFTPPort,
    String siteSFTPUsername,
    String siteSFTPPassword
  ) {
    this.sitePriority = sitePriority;
    this.siteSFTPDestination = siteSFTPDestination;
    this.siteSFTPPort = siteSFTPPort;
    this.siteSFTPUsername = siteSFTPUsername;
    this.siteSFTPPassword = siteSFTPPassword;
  }
}
