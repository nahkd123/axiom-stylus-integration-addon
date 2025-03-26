package io.github.nahkd123.axiomstylus.preset;

public enum BrushMode {
	WASH("Wash", "Paint on top of blocks in actual world"),
	BUILD_UP("Build up", "Paint on top of blocks in both actual world and in preview");

	private String name;
	private String description;

	private BrushMode(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() { return name; }

	public String getDescription() { return description; }
}
