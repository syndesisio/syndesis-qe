package io.syndesis.qe.pages.integrations;

public enum IntegrationStartingStatus {

    ASSEMBLING("Assembling ( 1 / 4 )"),
    BUILDING("Building ( 2 / 4 )"),
    DEPLOYING("Deploying ( 3 / 4 )"),
    STARTING("Starting ( 4 / 4 )");

    private String status = "";

    IntegrationStartingStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
