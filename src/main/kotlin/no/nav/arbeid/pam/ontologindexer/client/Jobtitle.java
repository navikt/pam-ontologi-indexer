package no.nav.arbeid.pam.ontologindexer.client;

public class Jobtitle implements ESObject {

    private String id;
    private String tekst;

    public Jobtitle(String id, String tekst) {
        this.id = id;
        this.tekst = tekst;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String asJson() {
        return null;
    }
}
