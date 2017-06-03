package com.gem.persistentparameter;


import hudson.model.*;


import org.kohsuke.stapler.*;



public class CurrentProject
{
  public static final AbstractProject<?, ?> getCurrentProject(ParameterDefinition thisParameter)
  {
    try
    {
      AbstractProject<?, ?> project = Stapler.getCurrentRequest().findAncestorObject(AbstractProject.class);
      
      if(project != null) {
    	System.out.println("Found some project");
        return Stapler.getCurrentRequest().getRequestURI().endsWith("/build") ? project : null;
      }
      else {
    	  System.out.println("Project is null. This seems to be a pipeline job");
    	  
    	 // WorkflowJob wj = Stapler.getCurrentRequest().findAncestorObject(WorkflowJob.class);
    	 
    	 // System.out.println("Env Vars " +  wj.getLastBuild().getEnvVars());
    	 
      }
    }
    catch(Exception ex)
    {
    	//ex.printStackTrace();
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
    	//ex.printStackTrace();
    }
    
    return null;
  }
}
