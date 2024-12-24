package server.model;

import gui.app.App;
import server.service.AccountService;

public class Account {
	private int id;
	private String name;
	private String group;
	private double balance;
	private double goal;

	// Constructors
	public Account() {
	}

	public Account(int id, String name, String group, double balance, double goal) {
		this.id = id;
		this.name = name;
		this.group = group;
		this.balance = balance;
		this.goal = goal;
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public double getGoal() {
		return goal;
	}
	
	public void setGoal(double goal) {
		this.goal = goal;
	}
	
	public double getTimeRelatedBalance () {
		App app = App.getInstance();
		AccountService accountService = new AccountService();
		
		if (app.latestTime()) return this.balance;

		return accountService.getBalance(this.id, app.getYear(), 12);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
