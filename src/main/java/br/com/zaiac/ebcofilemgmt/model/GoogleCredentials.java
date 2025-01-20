package br.com.zaiac.ebcofilemgmt.model;

public class GoogleCredentials {

  private String gcProject;
  private String gcsPie;
  private String keyDir;
  private String GoogleApplicationCredentials;

  public String getGcProject() {
    return gcProject;
  }

  public String getGcsPie() {
    return gcsPie;
  }

  public String getKeyDir() {
    return keyDir;
  }

  public String getGoogleApplicationCredentials() {
    return GoogleApplicationCredentials;
  }

  public GoogleCredentials(String gcProject, String gcsPie, String keyDir, String googleApplicationCredentials) {
    this.gcProject = gcProject;
    this.gcsPie = gcsPie;
    this.keyDir = keyDir;
    this.GoogleApplicationCredentials = googleApplicationCredentials;
  }
}
