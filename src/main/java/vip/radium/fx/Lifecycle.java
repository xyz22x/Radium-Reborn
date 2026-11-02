package vip.radium.fx;

public enum Lifecycle {
    FADE("FADE"),
    ANIMATION("ANIM");

    private final String tag;

    Lifecycle(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return this.tag;
    }
}
