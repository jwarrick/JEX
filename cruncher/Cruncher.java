package cruncher;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;

public class Cruncher {
	
	public volatile boolean stopGuiTask = false;
	public volatile boolean stopCrunch = false;
	
	List<Callable<Integer>> guiTasks;
	List<Ticket> tickets;
	private final ExecutorService guiTicketQueue = Executors.newFixedThreadPool(1);
	private final ExecutorService ticketQueue   = Executors.newFixedThreadPool(1);
	private ExecutorService multiFunctionQueue = Executors.newFixedThreadPool(5);
	private ExecutorService singleFunctionQueue = Executors.newFixedThreadPool(1);
	
	public Cruncher(){
		tickets = new Vector<Ticket>(0);
		guiTasks = new Vector<Callable<Integer>>(0);
		// I'm testing the commit and push process
	}
	
	public void runTicket(Ticket ticket)
	{
		this.stopCrunch = false;
		JEXStatics.logManager.log("Added ticket to running queue ",1,this);
		JEXStatics.statusBar.setStatusText("Added ticket to running queue ");
		ticketQueue.submit(ticket);
	}
	
	public Future<Integer> runFunction(FunctionCallable function, boolean multiThreading)
	{
		JEXStatics.logManager.log("Added function to cruncher queue ",1,this);
		JEXStatics.statusBar.setStatusText("Added function to cruncher queue ");
		Future<Integer> result = null;
		if(multiThreading)
		{
			result = multiFunctionQueue.submit(function);
		}
		else
		{
			result = singleFunctionQueue.submit(function);
		}
		return result;
	}
	
	public synchronized void finishTicket(Ticket ticket)
	{
		String str = "Crunch canceled, failed, or created no objects. No changes made.";
		if(ticket == null)
		{
			JEXStatics.statusBar.setStatusText(str);
			JEXStatics.logManager.log(str, 0, this);
			return;
		}
		TreeMap<JEXEntry,Set<JEXData>> outputList = ticket.outputList;
		if(outputList == null || outputList.size() == 0)
		{
			JEXStatics.statusBar.setStatusText(str);
			JEXStatics.logManager.log(str, 0, this);
			return;
		}
		JEXStatics.statusBar.setStatusText("Function successful. Creating output objects.");
		JEXStatics.logManager.log("Function successful. Creating output objects.", 0, this);
		JEXStatics.jexDBManager.saveDataListInEntries(outputList, true);
		
		if(ticket.getAutoSave())
		{
			JEXStatics.main.save();
		}
	}
	
	public Future<Object> runGuiTask(Callable<Object> guiTask)
	{
		this.stopGuiTask = false;
		JEXStatics.logManager.log("Added GUI task to running queue ",1,this);
		JEXStatics.statusBar.setStatusText("Added GUI task to running queue ");
		return guiTicketQueue.submit(guiTask);
	}
	
	public synchronized void finishImportThread(ImportThread importThread)
	{
		String str = "Import canceled, failed, or no objects created. No changes made.";
		boolean successful = false;
		if(importThread == null)
		{
			JEXStatics.statusBar.setStatusText(str);
			JEXStatics.logManager.log(str, 0, this);
			return;
		}
		TreeMap<JEXEntry,JEXData> toAdd = importThread.toAdd;
		if(toAdd == null || toAdd.size() == 0)
		{
			JEXStatics.statusBar.setStatusText(str);
			JEXStatics.logManager.log(str, 0, this);
			return;
		}
		successful = JEXStatics.jexDBManager.saveDataInEntries(toAdd);
		if(successful)
		{
			JEXStatics.statusBar.setStatusText("Objects imported successfully");
		}
		else
		{
			JEXStatics.statusBar.setStatusText("Import failed or created no objects. No changes made.");
			JEXStatics.logManager.log(str, 0, this);
		}
	}
	
}
