package org.carrot2.util;

import java.io.File;
import java.util.StringTokenizer;

import com.google.common.base.Strings;

/**
 * This class defines utilities methods helping to determine path-related
 * information such as relative paths.
 * 
 * The original code comes from <b>org.codehaus.plexus.util.PathTool<b>.
 * 
 * @author Ibrahim Chaehoi
 * 
 * Original Authors :
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class PathUtils {


    private PathUtils()
    {
        // Prevent Instantiation
    }

    /**
     * This method can calculate the relative path between two pathes on a file system.
     * <br/>
     * <pre>
     * PathUtils.getRelativeFilePath( null, null )                                   = ""
     * PathUtils.getRelativeFilePath( null, "/usr/local/java/bin" )                  = ""
     * PathUtils.getRelativeFilePath( "/usr/local", null )                           = ""
     * PathUtils.getRelativeFilePath( "/usr/local", "/usr/local/java/bin" )          = "java/bin"
     * PathUtils.getRelativeFilePath( "/usr/local", "/usr/local/java/bin/" )         = "java/bin"
     * PathUtils.getRelativeFilePath( "/usr/local/java/bin", "/usr/local/" )         = "../.."
     * PathUtils.getRelativeFilePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "java/bin/java.sh"
     * PathUtils.getRelativeFilePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = "../../.."
     * PathUtils.getRelativeFilePath( "/usr/local/", "/bin" )                        = "../../bin"
     * PathUtils.getRelativeFilePath( "/bin", "/usr/local/" )                        = "../usr/local"
     * </pre>
     * Note: On Windows based system, the <code>/</code> character should be replaced by <code>\</code> character.
     *
     * @param oldPath
     * @param newPath
     * @return a relative file path from <code>oldPath</code>.
     */
    public static final String getRelativeFilePath( final String oldPath, final String newPath )
    {
        if (Strings.isNullOrEmpty(oldPath) || Strings.isNullOrEmpty(newPath))
        {
            return "";
        }

        // normalise the path delimiters
        String fromPath = new File( oldPath ).getPath();
        String toPath = new File( newPath ).getPath();

        // strip any leading slashes if its a windows path
        if ( toPath.matches( "^\\[a-zA-Z]:" ) )
        {
            toPath = toPath.substring( 1 );
        }
        if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
        {
            fromPath = fromPath.substring( 1 );
        }

        // lowercase windows drive letters.
        if ( fromPath.startsWith( ":", 1 ) )
        {
            fromPath = Character.toLowerCase( fromPath.charAt( 0 ) ) + fromPath.substring( 1 );
        }
        if ( toPath.startsWith( ":", 1 ) )
        {
            toPath = Character.toLowerCase( toPath.charAt( 0 ) ) + toPath.substring( 1 );
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.
        if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
                        && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
        {
            // they both have drive path element but they dont match, no
            // relative path
            return null;
        }

        if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
                        || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
        {
            // one has a drive path element and the other doesnt, no relative
            // path.
            return null;
        }

        String resultPath = buildRelativePath( toPath, fromPath, File.separatorChar );

        if ( newPath.endsWith( File.separator ) && !resultPath.endsWith( File.separator ) )
        {
            return resultPath + File.separator;
        }

        return resultPath;
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    private static final String buildRelativePath( String toPath,  String fromPath, final char separatorChar )
    {
        // use tokeniser to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
        {
            if ( separatorChar == '\\' )
            {
                if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }
            else
            {
                if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }

            count++;
        }

        // reinitialise the tokenisers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        while ( count-- > 0 )
        {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuilder relativePath = new StringBuilder();

        // add back refs for the rest of from location.
        while ( fromTokeniser.hasMoreTokens() )
        {
            fromTokeniser.nextToken();

            relativePath.append("..");

            if ( fromTokeniser.hasMoreTokens() )
            {
                relativePath.append(separatorChar);
            }
        }

        if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
        {
            relativePath.append(separatorChar);
        }

        // add fwd fills for whatevers left of newPath.
        while ( toTokeniser.hasMoreTokens() )
        {
            relativePath.append(toTokeniser.nextToken());

            if ( toTokeniser.hasMoreTokens() )
            {
                relativePath.append(separatorChar);
            }
        }
        return relativePath.toString();
    }
}
