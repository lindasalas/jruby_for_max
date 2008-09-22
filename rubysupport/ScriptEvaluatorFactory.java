package ajm.rubysupport;

/*
Copyright (c) 2008, Adam Murray (adam@compusition.com). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ajm.util.MappedSet;

/**
 * Factory for Ruby evaluators that manages shared contexts.
 * 
 * @version 0.8
 * @author Adam Murray (adam@compusition.com)
 */
public class ScriptEvaluatorFactory {

	private static Map<String, ScriptEvaluator> contexts = new HashMap<String, ScriptEvaluator>();
	private static Map<String, Integer> contextCounter = new HashMap<String, Integer>();
	private static MappedSet<String, String> contextDestroyedListeners = new MappedSet<String, String>();
	private static MappedSet<String, String> contextMaxObjectMapping = new MappedSet<String, String>();

	private static Constructor<ScriptEvaluator> evaluatorConstructor;

	private static ScriptEvaluator newRubyEvaluatorInstance() {
		if (evaluatorConstructor == null) {
			String evaluatorClassName = RubyProperties.getRubyEngine();
			try {
				@SuppressWarnings("unchecked")
				Class<ScriptEvaluator> evaluatorClass = (Class<ScriptEvaluator>) Class.forName(evaluatorClassName);
				evaluatorConstructor = evaluatorClass.getConstructor();
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			return evaluatorConstructor.newInstance();
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private ScriptEvaluatorFactory() {

	}

	public static ScriptEvaluator getRubyEvaluator(String context, String maxObjVar) {
		ScriptEvaluator evaluator = contexts.get(context);
		if (evaluator == null) {
			evaluator = newRubyEvaluatorInstance();
			contexts.put(context, evaluator);
			contextCounter.put(context, 1);
			// evaluator.declarePersistentGlobal("MaxObjects", obj)
		}
		else {
			int count = contextCounter.get(context);
			count++;
			contextCounter.put(context, count);
		}
		contextMaxObjectMapping.addValue(context, maxObjVar);
		return evaluator;
	}

	/**
	 * Unregisters a RubyEvaluator.
	 * 
	 * @param context
	 *            the evaluator's shared context name
	 * @return true if the entire context was removed
	 */
	public static void removeRubyEvaluator(String context, String maxObjVar) {
		int count = contextCounter.get(context);
		count--;
		if (count > 0) {
			contextCounter.put(context, count++);
			contextMaxObjectMapping.get(context).remove(maxObjVar);
		}
		else {
			notifyContextDestroyedListener(context);
			contextDestroyedListeners.remove(context);
			contextMaxObjectMapping.remove(context);
			contextCounter.remove(context);
			contexts.remove(context);
		}
	}

	public static Collection<String> getMaxObjectVariables(String context) {
		return contextMaxObjectMapping.get(context);
	}

	public static void registerContextDestroyedListener(String context, String callbackMethod) {
		contextDestroyedListeners.addValue(context, callbackMethod);
	}

	public static void notifyContextDestroyedListener(String context) {
		ScriptEvaluator ruby = contexts.get(context);
		Collection<String> callbackMethods = contextDestroyedListeners.remove(context);
		// remove() lets the garbage collector do its job

		if (ruby != null && callbackMethods != null) {
			for (String callbackMethod : callbackMethods) {
				try {
					ruby.eval(callbackMethod);
				}
				catch (Exception e) {
					System.err.println("on_context_destroyed method '" + callbackMethod + "' failed:\n"
							+ e.getMessage());
				}
			}
		}
	}
}