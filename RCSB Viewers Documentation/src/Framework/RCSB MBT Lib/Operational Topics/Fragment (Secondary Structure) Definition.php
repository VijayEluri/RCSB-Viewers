<?php
  session_start();
  set_include_path($_SESSION['INCLUDE_PATH']);
  include_once "resources/snippets/prefix.php";
?>
    <ul class="preface">
      <li>
	      If <span class="methodname">deriveFragments()</span> throws an exception, it tries a
	      <span class="methodname">loadFragments()</span>, again. Might be just to
	      clear everything out?
      </li>
      <li>
      	In the loaders, conformation information is ignored. Fragments are <em>always</em> derived.
      </li>
    </ul>
    
    <ul class="relevent-classes">
      <li>Structure</li>
      <li>StructureMap</li>
      <li>Conformation* - intermediate container for various conformation types (COIL, HELIX, etc.)</li>
      <li>StructureComponent* - primarily <span class="classname">Fragment</span>, in this discussion.</li>
      <li>RangeMap</li>
      <li>Range</li>
      <li>DerivedInformation</li>
      <li>Fragment</li>          
    </ul>
    <p>
      Look in the <span class="projectname">RCSB MBT Libs</span> project, in the source dir
      <span class="foldername">Structure Model</span>, package <span class="packagename">org.rcsb.mbt.model</span> for most
      of this (unless otherwise specified).
    </p>
    <p>
      <span class="classname">Structure</span> is an abstract class. The loaders derive a helper class from it, and use it to push off all their
      discovered records, without analysis.
    </p>
    <p>
      <span class="classname">StructureMap</span> is the real core of the structure model. The information kept here is what is actually contains
      the atom/bond/fragment relationships (The raw types have been moved to <span class="packagename">org.rcsb.mbt.model.interim</span>).
    </p>
    <p>
      First, any definitions that are picked up in the file are kept in a list along with all of the other
      <span class="classname">StructureComponent</span>-derived items defined there (Atoms, Residues, Chains, Bonds). This list is kept in the
      <span class="classname">Structure</span> class (abstract class derived by loader into a loader-specific implementation). They simply consist
      of raw information as they were collected from the file. These classes (<span class="classname">Coil</span>, <span class="classname">Helix</span>,
      <span class="classname">Strand</span>, <span class="classname">Turn</span>),
      derive from <span class="classname">Conformation</span> (which is derived from <span class="classname">StructureComponent</span>).
    </p>
    <p>
      If they exist, these records are examined (in <span class="classname">StructureMap</span> - look for <span class="methodname">generateFragments()</span> and
      <span class="methodname">loadFragments()</span>). An intermediate type called <span class="classname">RangeMap</span> is used to store residue ranges for each Conformation
      type found.
    </p>
    <p>
      If they don't exist, then <span class="methodname">deriveFragments()</span> is called, which creates a
      <span class="classname">org.rcsb.mbt.model.util.DerivedInformation</span> object used to synthesize the ranges through a heuristic
      <em>Kabsch-Sander</em> is the algorithm cited in the comments.) Basically, it consists of subdividing ranges until the
      conformation is determined. Note the 'Ss'-prefix helper classes. ('Ss' stands for 'SecondaryStructure').
    </p>
    <p>
      Finally, the completed <span class="classname">Range</span> objects are traversed and turned into <span class="classname">Fragment</span>
      types, which is the destination
      type and is what ultimately ends up in the <span class="classname"> StructureMap lists. Each fragment has a
      <span class="enumeration">ConformationType</span> (which is
      just another <span class="enumeration">ComponentType</span>) set to indicate what conformation it is,
      and a list of residues that make it up.
    </p>

<?php
  include_once "resources/snippets/suffix.php";
?>