package com.gem.persistentparameter;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

/**
 * @author huybrechts
 */
public class PersistentChoiceParameterDefinition extends SimpleParameterDefinition
{
  private static final long serialVersionUID = -2000203724582104709L;
  public static final String CHOICES_DELIMETER = "\\r?\\n";

  private final List<String> choices;
  private final String defaultValue;
  private final boolean successfulOnly;

  public static boolean areValidChoices(String choices)
  {
    String strippedChoices = choices.trim();
    return !StringUtils.isEmpty(strippedChoices) && strippedChoices.split(CHOICES_DELIMETER).length > 0;
  }

  @DataBoundConstructor
  public PersistentChoiceParameterDefinition(String name, String choices, boolean successfulOnly, String description)
  {
    super(name, description);
    this.choices = new ArrayList<String>(Arrays.asList(choices.split(CHOICES_DELIMETER)));
    defaultValue = null;
    this.successfulOnly = successfulOnly;
  }

  private PersistentChoiceParameterDefinition(String name, List<String> choices, String defaultValue, boolean successfulOnly, String description)
  {
    super(name, description);
    this.choices = choices;
    this.defaultValue = defaultValue;
    this.successfulOnly = successfulOnly;
  }

  @Override
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue)
  {
    if(defaultValue instanceof StringParameterValue)
    {
      StringParameterValue value = (StringParameterValue)defaultValue;
      return new PersistentChoiceParameterDefinition(getName(), getChoices(), value.value, successfulOnly, getDescription());
    }
    else
    {
      return this;
    }
  }

  @Exported
  public List<String> getChoices()
  {
    String def = defaultValue;
    try
    {
      ParameterValue lastValue = CurrentProject.getLastValue(this, successfulOnly);
      def = ((StringParameterValue)lastValue).value;
    }
    catch(Exception ex)
    {
    }
    
    if(def != null && choices.indexOf(def) != 0)
    {
      List<String> c = new ArrayList<String>(choices);
      c.remove(def);
      c.add(0, def);
      return c;
    }
    
    return choices;
  }

  public String getChoicesText()
  {
    return StringUtils.join(getChoices(), "\n");
  }

  public boolean isSuccessfulOnly()
  {
    return successfulOnly;
  }

  @Override
  public StringParameterValue getDefaultParameterValue()
  {
    return new StringParameterValue(getName(), defaultValue == null ? getChoices().get(0) : defaultValue, getDescription());
  }

  private StringParameterValue checkValue(StringParameterValue value)
  {
    if(!choices.contains(value.value))
      throw new IllegalArgumentException("Illegal choice: " + value.value);
    return value;
  }

  @Override
  public ParameterValue createValue(StaplerRequest req, JSONObject jo)
  {
    StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
    value.setDescription(getDescription());
    return checkValue(value);
  }

  public StringParameterValue createValue(String value)
  {
    return checkValue(new StringParameterValue(getName(), value, getDescription()));
  }

  @Extension
  public static class DescriptorImpl extends ParameterDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return "Persistent Choice Parameter";
    }

    @Override
    public String getHelpFile()
    {
      return "/help/parameter/choice.html";
    }

    /**
     * Checks if parameterised build choices are valid.
     */
    public FormValidation doCheckChoices(@QueryParameter String value)
    {
      if(PersistentChoiceParameterDefinition.areValidChoices(value))
      {
        return FormValidation.ok();
      }
      else
      {
        return FormValidation.error("Invalid value");
      }
    }
  }
}