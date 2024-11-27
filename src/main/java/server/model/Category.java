package server.model;

public class Category {
	private int id;
	private String name;
	private Double budget; // Budget can be null
	private String type;

	// Constructors
	public Category() {
		name = "";
	}

	public Category(int id, String name, Double budget, String type) {
		this.id = id;
		this.name = name;
		this.budget = budget;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getBudget() {
		return budget;
	}

	public void setBudget(Double budget) {
		this.budget = budget;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
