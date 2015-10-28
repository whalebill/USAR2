package edu.junyao.usar.element;

public class Note {
	private boolean shown;
	private boolean updated;
	
	public Note(){
		shown = false;
		updated = false;
	}

	public boolean isShown() {
		return shown;
	}

	public void setShown(boolean shown) {
		this.shown = shown;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
}
