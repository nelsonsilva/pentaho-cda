package pt.webdetails.cda.dataaccess;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.dom4j.Element;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 4:09:48 PM
 */
public class Parameter
{

  private String name;
  private String type;
  private String defaultValue;
  private String pattern;
  private String stringValue;

  public Parameter()
  {
  }

  public Parameter(final String name, final String type, final String defaultValue, final String pattern)
  {
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
    this.pattern = pattern;
  }

  public Parameter(final Element p)
  {
    this(
        p.attributeValue("name"),
        p.attributeValue("type"),
        p.attributeValue("default"),
        p.attributeValue("pattern")
    );
  }

  public Parameter(final String name, final String stringValue)
  {
    this.name = name;
    this.stringValue = stringValue;
  }


  public Object getValue() throws InvalidParameterException
  {
    // This depends on the value
    final String localValue = getStringValue() == null ? getDefaultValue() : getStringValue();

    if (getType().equals("String"))
    {
      return localValue;
    }
    else if (getType().equals("Integer"))
    {
      return Integer.parseInt(localValue);
    }
    else if (getType().equals("Numeric"))
    {
      return Double.parseDouble(localValue);
    }
    else if (getType().equals("Date"))
    {
      SimpleDateFormat format = new SimpleDateFormat(getPattern());
      try
      {
        return format.parse(localValue);
      }
      catch (ParseException e)
      {
        throw new InvalidParameterException("Unable to parse date " + localValue + " with pattern " + getPattern() , e);
      }
    }
    else{
      throw  new InvalidParameterException("Parameter type " + getType() + " unknown, can't continue",null);
    }


  }


  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(final String type)
  {
    this.type = type;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(final String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getPattern()
  {
    return pattern;
  }

  public void setPattern(final String pattern)
  {
    this.pattern = pattern;
  }

  public String getStringValue()
  {
    return stringValue;
  }

  public void setStringValue(final String stringValue)
  {
    this.stringValue = stringValue;
  }

}
