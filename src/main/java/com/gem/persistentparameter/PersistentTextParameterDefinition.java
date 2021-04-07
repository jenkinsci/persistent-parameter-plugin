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
import hudson.model.ParameterValue;
import hudson.model.TextParameterValue;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link PersistentStringParameterDefinition} that uses textarea, instead of text box.
 */
public class PersistentTextParameterDefinition extends PersistentStringParameterDefinition
{
  private static final long serialVersionUID = -2399747352939324797L;

  @DataBoundConstructor
  public PersistentTextParameterDefinition(String name, String defaultValue, boolean successfulOnly, String description)
  {
    super(name, defaultValue, successfulOnly, description);
  }

  @Extension
  @Symbol({"persistentText", "persistentTextParam"})
  public static class DescriptorImpl extends ParameterDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return "Persistent Text Parameter";
    }
  }

  @Override
  public ParameterValue createValue(StaplerRequest req, JSONObject jo)
  {
    TextParameterValue value = req.bindJSON(TextParameterValue.class, jo);
    value.setDescription(getDescription());
    return value;
  }

  @Override
  public ParameterValue createValue(String value)
  {
    return new TextParameterValue(getName(), value, getDescription());
  }
}
