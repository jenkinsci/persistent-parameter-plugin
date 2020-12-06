/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Luca Domenico Milanesio, Seiji Sogabe, Tom Huybrechts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.gem.persistentparameter;

import hudson.Extension;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterDefinition.ParameterDescriptor;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parameter whose value is a string value.
 */
public class PersistentStringParameterDefinition extends SimpleParameterDefinition
{
  private static final long serialVersionUID = -7077702646937544392L;
  private final String defaultValue;
  private final boolean successfulOnly;
  private final boolean trim;

  @DataBoundConstructor
  public PersistentStringParameterDefinition(String name, String defaultValue, boolean successfulOnly, String description, boolean trim)
  {
    super(name, description);
    this.defaultValue = defaultValue;
    this.successfulOnly = successfulOnly;
    this.trim = trim;
  }

  public PersistentStringParameterDefinition(String name, String defaultValue, boolean successfulOnly, String description)
  {
    this(name, defaultValue, successfulOnly, description, false);
  }

  @Override
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue)
  {
    if(defaultValue instanceof StringParameterValue)
    {
      StringParameterValue value = (StringParameterValue)defaultValue;
      return new PersistentStringParameterDefinition(getName(), (String)value.getValue(), successfulOnly, getDescription(), trim);
    }
    else
    {
      return this;
    }
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  /**
   * @return trim - {@code true}, if trim options has been selected, else return {@code false}. Trimming will happen when creating
   * {@link StringParameterValue}s, the value in the config will not be changed.
   */
  public boolean isTrim()
  {
    return trim;
  }

  public boolean isSuccessfulOnly()
  {
    return successfulOnly;
  }

  /**
   * @return original or trimmed defaultValue (depending on trim)
   */
  @Override
  public StringParameterValue getDefaultParameterValue()
  {
    ParameterValue lastValue = CurrentProject.getLastValue(this, successfulOnly);
    String str = (lastValue instanceof StringParameterValue) ? (String)((StringParameterValue)lastValue).getValue() : defaultValue;

    StringParameterValue value = new StringParameterValue(getName(), str, getDescription());
    doTrim(value);
    return value;
  }

  @Override
  public ParameterValue createValue(StaplerRequest req, JSONObject jo)
  {
    StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
    doTrim(value);
    value.setDescription(getDescription());
    return value;
  }

  @Override
  public ParameterValue createValue(String str)
  {
    StringParameterValue value = new StringParameterValue(getName(), str, getDescription());
    doTrim(value);
    return value;
  }

  private void doTrim(StringParameterValue value)
  {
    if(trim)
    {
      value.doTrim();
    }
  }

  @Extension
  @Symbol({"persistentString", "persistentStringParam"})
  public static class DescriptorImpl extends ParameterDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return "Persistent String Parameter";
    }

    @Override
    public String getHelpFile()
    {
      return "/help/parameter/string.html";
    }
  }
}
