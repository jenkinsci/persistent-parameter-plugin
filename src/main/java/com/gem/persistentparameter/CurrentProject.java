package com.gem.persistentparameter;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;

import org.kohsuke.stapler.Stapler;

public class CurrentProject
{
  public static final AbstractProject<?, ?> getCurrentProject(ParameterDefinition thisParameter)
  {
    try
    {
      AbstractProject<?, ?> project = Stapler.getCurrentRequest().findAncestorObject(AbstractProject.class);
      if(project != null)
        return Stapler.getCurrentRequest().getRequestURI().endsWith("/build") ? project : null;
    }
    catch(Exception ex)
    {
    }
    
    try
    {
      for(AbstractProject<?, ?> project: Hudson.getInstance().getAllItems(AbstractProject.class))
      {
        ParametersDefinitionProperty property = project.getProperty(ParametersDefinitionProperty.class);
        if(property != null)
          for(ParameterDefinition parameter: property.getParameterDefinitions())
            if(parameter == thisParameter)
              return project;
      }
    }
    catch(Exception ex)
    {
    }
    
    return null;
  }
}
