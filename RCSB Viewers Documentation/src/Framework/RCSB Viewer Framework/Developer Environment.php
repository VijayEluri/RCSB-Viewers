<?php
  session_start();
  set_include_path($_SESSION['INCLUDE_PATH']);
  include_once "resources/snippets/prefix.php";
?>
<p>
We are currently using <em>Eclipse</em> for our build environment.  Other environments may work,
but since Eclipse is universally available and free, we highly recommend your using it.</p>
<p>
The rest of these instructions are presuming you are using Eclipse.</p>
<dl>
<dt>Install Java JDK</dt>
<dd>
On the Mac, JDK 1.5 is installed for you.  Other platforms will vary.  In general, you want to install
JDK1.6, if it's available on your architecture.  Make sure the 'JAVAHOME' environment points to your
JDK directory, and that 'JAVAHOME/bin' is in your path.  You should be able to run 'java -version'
from the commandline from a fresh login without having to do anything else.</dd>
<dt>Install Eclipse</dt>
<dd>
You can download the latest version (<em>Ganymede, as of this writing</em>) at the
<a href="http://www.eclipse.org/">Eclipse</a> website.  Download the one for your OS/architecture
and follow the instructions to install.</dd>
<dt>Add <em>SVN</em> plugin.</dt>
<dd>
We use a specific SVN release - don't use just a generic release:
<ul>
<li>
Under the 'Help' menu item, go to 'Software Updates'.</li>
<li>
Go to the 'Available Software' tab.</li>
<li>
Hit the 'Add' button.</li>
<li>
Enter this URL in the prompt: <em>http://subclipse.tigris.org/</em></li>
<li>
After clicking 'Ok', the 'subclipse.tigris.org' selection will appear in the tree list.
Expand that and select any 'required' <em>(Subclipse, SVNKit Adapter)</em> settings, as well
as the following:
<ul>
<li><em>JAVAHL Adapter</em>.  Use the Java native version for Mac or Linux.  Optionally
select the jni version for Windows.</li>
</ul>
</li>
<li>
Click 'Install'.  You'll probably want to restart Eclipse.</li>
</ul>
</dd>
<dt>Add the 'pdbcvs' repository</dt>
<dd>
You should now be able to get to the SVN perspective (select from dropdown in upper right.)
In this perspective, right click the left panel and select 'New/Repository Location'.
<ul>
<li>
Enter 'svn+ssh://pdbcvs.rcsb.org/misc/pdb/svn' in the prompt and 'Ok'.</li>
<li>
Eclipse will further prompt you for your username and password for the 'pdbcvs' account.
Enter them.  You will likely want Eclipse to retain your password - otherwise, it will prompt
you for a username and password for every operation.</li>
<li>
The repository should now show up in the SVN panel.  You should be able to expand the
tree and look at the various projects.</li>
</ul>
</dd>
<dt>
Add the 'TestNG' plugin</dt>
<dd>
We have incorporated the <em>TestNG</em> unit testing framework.  Unfortunately, that means the project will not
build unless you add the plug-in.  To do that:
<ul>
<li>
Go to the 'Help/Software Updates/Available Software' panel, again.</li>
<li>
Click the 'Add' Button.</li>
<li>
In the prompt, enter: <em>http://beust.com/eclipse</em></li>
<li>
Expand the new entry and you will see 'TestNG'.  Check that and hit the Install button.</li>
<li>
After installing, Eclipse will suggest you restart it.  Probably a good idea.</li>
</ul>
</dd>
<dt>Check Out the Viewers Projects</dt>
<dd>
In the repository item, expand the 'RCSB Viewers' entry.  You will see three subdirectories:
<ul>
<li>branches</li>
<li>tags</li>
<li>trunk</li>
</ul>
For the latest stable release, expand 'tags' and the last entry in tags.</dd>
<dd>
For continuing development, either create a new branch or select the trunk, depending on your requirements.</dd>
<dd>
At this point, you will see the list of viewer projects.  Select them all, right click on the selection, and hit
'Check Out' from the dropdown.  This can take some time, depending on where you are and what OS you're using.
Best to do it at night before you go to bed.</dd>
</dl>
<h1>Other Build/OS Specific Tweaks</h1>
<p>
The following may be necessary, depending on your environment:</p>
<dl>
<dt>All</dt>
<dd>
Generally, you want to make sure the 'Project/Build Automatically' menuitem is checked.  You can trigger a
build manually from the menu, if you like.</dd>
<dl>
<dt>Jogl (Java OpenGL)</dt>
<dd>
Because there is a native component to Jogl, there are some fiddly aspects to configuring.  Almost all
of the time there is a problem with a viewer, it's because it can't create an OpenGL viewer and
that's because it can't find the respective jnilib.</dd>
<dd>
If you're having problems, make sure the following is set in the 'Build Path' configuration panel of
<em>all</em> the viewer projects:
<ul>
<li>In the 'Libraries' tab, <em>gluegen-rt.jar</em> and <em>jogl.jar</em> should be listed.
If not, add them by hitting the 'Add Jars' button and expand the tree in the prompt box to
'3rd Pary Libs/jogl' (ignore the versioned entries - they are there for backup purposes)
 and selecting the jars.  Click 'Ok' and the new jar entries will appear in the list.</li>
<li>In each one of these entries, the 'Native library location' setting should be set to:
 '3rd Party Libs/jogl/jnilibs' (expand the item to see the setting.)</li>
</ul>
</dl>
<dt>Mac</dt>
<dd>
Should build and be runnable/debuggable, immediately.  As of this writing, Mac is only actively supporting JDK version
1.5, although 1.6 can be installed as an option.  If you change it, make sure you update all of the Eclipse
project settings pertaining to JDK compile and build environment.</dd>
<dt>Linux & Windows</dt>
<dd>
The MBT lib needs the JAI jars to build - these are provided by default in the Mac JDK, but not on Linux and Windows.
To add them:
<ul>
<li>Right-click on the 'RCSB MBT Libs' project and select 'Build Path/Configure Build Path' from 
the drop-down.</li>
<li>
Expand the 'Jai' entry (ignore the versioned entries, they are there for backup purposes) and 
select the <em>jai_codec.jar</em> and <em>jai_core.jar</em> entries.  On pressing ok, these will
be added to the path list.</li>
<li>
Nothing further needs to be done.</li>
</ul>
</dd>
<dt>Linux64/Win64/Sparc/Other</dt>
<dd>
The 64bit native shared library names for the Jogl libs unfortunately conflict with the 32bit
native library names, so there's not a real graceful way to handle this, at the moment.</dd>
<dd>
The way I've been doing it is to go down into the 'RCSB Viewer Jars' project and down to the
'3rd Party Libs/jnilibs' path in that project (this project defines a standalone runtime
environment, which is why it is here.)</dd>
<dd>
Here, you will find the original distribution
zip files for each architecture.  Unzip the file that corresponds to your architecture and copy
the libs from the resulting '&lt;libarch...&gt;/lib/' directory to the local (jnilibs) directory and
to the '../../3rd Party Libs/jogl/jnilibs' directory.</dd>
<dd>
(Expect this to get reorganized, in the future.)</dd>
<dd>
At this point, you should be able to debug or run the TestRun scripts.</dd>
<?php
  include_once "resources/snippets/suffix.php";
?>