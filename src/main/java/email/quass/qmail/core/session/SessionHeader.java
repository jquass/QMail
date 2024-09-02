package email.quass.qmail.core.session;

public enum SessionHeader {
    SESSION_ID("X-SESSION-ID"),
    USERNAME("X-USERNAME"),
    ;

    private final String header;

    SessionHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
