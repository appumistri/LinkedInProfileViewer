package profileViewer;

public enum Position {
	TOP(1), BOTTOM(0);

	private int position;

	Position(int position) {
		this.position = position;
	}

	int getPosition() {
		return this.position;
	}
}
