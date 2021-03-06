package com.gem.persistentparameter;

import hudson.Extension;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class PersistentChoiceParameterDefinition extends SimpleParameterDefinition
{
  private static final long serialVersionUID = -2000203724582104709L;
  public static final String CHOICES_DELIMITER = "\\r?\\n";
  @Deprecated
  public static final String CHOICES_DELIMETER = CHOICES_DELIMITER;

  private /*semi-final*/ List<String> choices;
  private final String defaultValue;
  private /*semi-final*/ boolean successfulOnly;

  public static boolean areValidChoices(String choices)
  {
    String strippedChoices = choices.trim();
    return !strippedChoices.isEmpty() && strippedChoices.split(CHOICES_DELIMITER).length > 0;
  }

  public PersistentChoiceParameterDefinition(@Nonnull String name, @Nonnull String choices, boolean successfulOnly, String description)
  {
    this(name, splitChoices(choices), null, successfulOnly, description);
  }

  private PersistentChoiceParameterDefinition(@Nonnull String name, @Nonnull List<String> choices, String defaultValue, boolean successfulOnly, String description)
  {
    super(name, description);
    this.choices = choices;
    this.defaultValue = defaultValue;
    this.successfulOnly = successfulOnly;
  }

  /**
   * Databound constructor for reflective instantiation.
   *
   * @param name parameter name
   * @param description parameter description
   */
  @DataBoundConstructor
  @Restricted(NoExternalUse.class) // there are specific constructors with String and List arguments for 'choices'
  public PersistentChoiceParameterDefinition(String name, String description)
  {
    super(name, description);
    this.choices = new ArrayList<>();
    this.defaultValue = null;
    this.successfulOnly = false;
  }

  /**
   * Set the list of choices. Legal arguments are String (in which case the arguments gets split into lines) and Collection which sets the list of
   * legal parameters to the String representations of the argument's non-null entries.
   *
   * See JENKINS-26143 for background.
   *
   * This retains the compatibility with the legacy String 'choices' parameter, while supporting the list type as generated by the snippet generator.
   *
   * @param choices String or Collection representing this parameter definition's possible values.
   */
  @DataBoundSetter
  @Restricted(NoExternalUse.class) // this is terrible enough without being used anywhere
  public void setChoices(Object choices)
  {
    if(choices instanceof String)
    {
      this.choices = splitChoices((String)choices);
    }
    else if(choices instanceof List)
    {
      this.choices = ((List<?>)choices).stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.toList());
    }
    else
    {
      throw new IllegalArgumentException("Expected String or List, but got " + choices.getClass().getName());
    }
  }

  private static List<String> splitChoices(String choices)
  {
    return Arrays.asList(choices.split(CHOICES_DELIMITER));
  }

  @Override
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue)
  {
    if(defaultValue instanceof StringParameterValue)
    {
      StringParameterValue value = (StringParameterValue)defaultValue;
      return new PersistentChoiceParameterDefinition(getName(), getChoices(), (String)value.getValue(), successfulOnly, getDescription());
    }
    else
    {
      return this;
    }
  }

  @Exported
  public List<String> getChoices()
  {
    return choices;
  }

  public String getChoicesText()
  {
    return StringUtils.join(choices, "\n");
  }

  @DataBoundSetter
  public void setSuccessfulOnly(boolean successfulOnly)
  {
    this.successfulOnly = successfulOnly;
  }

  public boolean isSuccessfulOnly()
  {
    return successfulOnly;
  }

  @Override
  public StringParameterValue getDefaultParameterValue()
  {
    ParameterValue lastValue = CurrentProject.getLastValue(this, successfulOnly);
    String value = (lastValue != null) ? (String)((StringParameterValue)lastValue).getValue() : defaultValue;

    if(value == null)
    {
      if(choices.isEmpty())
      {
        return null;
      }
      value = choices.get(0);
    }

    return new StringParameterValue(getName(), value, getDescription());
  }

  private StringParameterValue checkValue(StringParameterValue value)
  {
    if(!isValid(value))
    {
      throw new IllegalArgumentException("Illegal choice for parameter " + getName() + ": " + value.getValue());
    }
    return value;
  }

  /**
   * Overrides {@link ParameterValue#isValid} that doesn't exist before Jenkins 2.244.
   */
  public boolean isValid(ParameterValue value)
  {
    return choices.contains((String)((StringParameterValue)value).getValue());
  }

  @Override
  public ParameterValue createValue(StaplerRequest req, JSONObject jo)
  {
    StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
    value.setDescription(getDescription());
    return checkValue(value);
  }

  @Override
  public StringParameterValue createValue(String value)
  {
    return checkValue(new StringParameterValue(getName(), value, getDescription()));
  }

  @Extension
  @Symbol({"persistentChoice", "persistentChoiceParam"})
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

    /*
     * We need this for JENKINS-26143 -- reflective creation cannot handle setChoices(Object). See that method for context.
     */
    @Override
    public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException
    {
      String name = formData.getString("name");
      String choicesText = formData.getString("choices");
      String description = formData.getString("description");
      boolean successfulOnly = formData.getBoolean("successfulOnly");

      return new PersistentChoiceParameterDefinition(name, choicesText, successfulOnly, description);
    }

    /**
     * Checks if parameterised build choices are valid.
     */
    public FormValidation doCheckChoices(@QueryParameter String value)
    {
      return PersistentChoiceParameterDefinition.areValidChoices(value) ? FormValidation.ok() : FormValidation.error("Requires Choices.");
    }
  }
}
