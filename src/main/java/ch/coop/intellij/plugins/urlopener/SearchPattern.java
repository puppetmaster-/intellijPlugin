package ch.coop.intellij.plugins.urlopener;

public class SearchPattern {
    private String name;
    private String shortcut;
    private String url;

    public SearchPattern(String name, String shortcut, String url) {
        this.name = name != null ? name : "New Pattern";
        this.shortcut = shortcut != null ? shortcut : "shortcut";
        this.url = url != null ? url : "https://example.com/%s";
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}