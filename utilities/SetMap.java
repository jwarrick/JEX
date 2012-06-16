package utilities;

import signals.Observable;
import signals.SSCenter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

import signals.Observer;
import java.util.HashMap;

public class SetMap<E extends Observable> implements Observer {

	/**
	 * This class uses getters and setters to file and access
	 * each item in the dictionary. Just set up which accessors
	 * to use and 'set' is automatically applied to be able to
	 * change the item if the setter is ever called.
	 */
	
// ASK ERWIN WHETHER THE DEFAULT HASH IS ADEQUATE FOR THE HASHSETS
	private HashMap<XKey,HashSet<E>> dictionary;
	private HashSet<String> accessorNames;
	
	public SetMap(String[] accessorNames)
	{
		this.accessorNames = new HashSet<String>();
		for(int i = 0; i < accessorNames.length; i++)
		{
			this.accessorNames.add(accessorNames[i]);
		}
		this.dictionary = new HashMap<XKey,HashSet<E>>();
	}
	
	public void put(E obj)
	{
		if(obj == null)
		{
			return;
			// Print error message here.
		}
		
		XKey xkey = this.keyForObject(obj);
		if(xkey == null) return;
		HashSet<E> currentSet = this.getSet(xkey);
		
		// Create new set if necessary
		if(currentSet == null)
		{
			currentSet = new HashSet<E>();
			this.dictionary.put(xkey, currentSet);
		}
		
		currentSet.add(obj);
		
		this.observe(obj);
	}
	
	// ASK ERWIN WHETHER THE DEFAULT HASH IS ADEQUATE FOR THE HASHSETS
	public void remove(E obj)
	{
		XKey xkey = this.keyForObject(obj);
		if(xkey == null) return;
		HashSet<E> currentSet = this.getSet(xkey);
		if(currentSet == null) return;
		currentSet.remove(obj);
		
		// Remove set if necessary
		if(currentSet.size() == 0) this.dictionary.remove(xkey);
		
		this.unobserve(obj);
	}
	
	public void removeAll()
	{
		Iterator<XKey> key = this.iterator();
		while(key.hasNext())
		{
			HashSet<E> set = this.getSet(key.next());
			for(E item : set)
			{
				this.unobserve(item);
			}
		}
		this.dictionary.clear();
	}
	
	public HashSet<E> getSet(XKey xkey)
	{
		return this.dictionary.get(xkey);
	}
	
	public Iterator<XKey> iterator()
	{
		return this.dictionary.keySet().iterator();
	}
	
	public HashSet<String> accessorNames()
	{
		return this.accessorNames;
	}
	
	public int size()
	{
		return this.dictionary.size();
	}
	
	private void observe(E obj)
	{
		if(this.accessorNames.size() > 0) SSCenter.observe(this, obj);
	}
	
	private void unobserve(E obj)
	{
		SSCenter.unobserve(this, obj);
	}
	
	private XKey keyForObject(E o)
	{
		XKey xkey = new XKey();
		Method m;
		Object key;
		
		Iterator<String> itr = this.accessorNames.iterator();
		while(itr.hasNext())
		{
			try {
				String accessor = itr.next();
				m = o.getClass().getMethod(accessor, (Class[])null);
				if(m != null)
				{
					key = m.invoke(o, (Object[])null);
					if(key != null)
					{
						xkey.putKey(accessor, key);
					}
					else
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return null;
			} catch (SecurityException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		}
		if(xkey.size() != this.accessorNames.size()) return null;
		return xkey;
	}
	
	@SuppressWarnings("unchecked")
	public void changed(Observable obj, String accessorName)
	{
		if(!this.accessorNames.contains(accessorName)) return;
		this.put((E)obj);
		System.out.println("Changed: " + obj.toString());
	}
	
	@SuppressWarnings("unchecked")
	public void changeAborted(Observable obj, String accessorName)
	{
		if(!this.accessorNames.contains(accessorName)) return;
		this.put((E)obj);
		System.out.println("Change Aborted: " + obj.toString());
	}

	@SuppressWarnings("unchecked")
	public void changing(Observable obj, String accessorName)
	{
		if(!this.accessorNames.contains(accessorName)) return;
		this.remove((E)obj);
		this.observe((E)obj);
		System.out.println("Changing: " + obj.toString());
	}
	
	@Override
	public String toString()
	{
//		String ret = "";
//		Iterator<XKey> itr = this.iterator();
//		while(itr.hasNext())
//		{
//			XKey tempKey = itr.next();
//			ret = ret + "\n";
//			ret = ret + 
//		}
		System.out.println(this.size());
		return this.dictionary.toString();
	}
	
}
