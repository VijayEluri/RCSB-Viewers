<?php
  session_start();
  set_include_path($_SESSION['INCLUDE_PATH']);
  include_once "resources/snippets/prefix.php";
?>
  <ul class="preface">
    <li>
      Probably the most confusing aspect of the loading/model creation mechanism.  See John Beaver's
      notes, below.
    </li>
    <li>
      Ids stored in the model are <em>Ndb</em> ids, not Pdb.  Pdb ids are looked up.
    </li>
    <li>
      One problem is the conversion methods are quite hard to use - they return a two-element array of
      objects which have to be tested for existence and cast.  I'm currently working on providing
      simplified versions.
    </li>
  </ul>
  
  <p>
  <em>Ndb</em> ids primarily come from .cif/.xml files, Pdb ids from .pdb files.  The identification schemes
  are quite different.</p>
  
  <p>
  Thus, the requirement to map from one to the other.  The <span class="classname">PdbToNdbConverter</span>
  performs this conversion.</p>
  
  <ul>
    <li>
      On loading XML files, the chain and residue ids are extracted in both Ndb and Pdb namespaces.
    </li>
    <li>
      On loading PDB files, the Ndb ids are set to their corresponding Pdb ids, thus the mapping is essentially 1:1.
    </li>
  </ul>
  
  <p>
  The loaders create
  the <span class="classname">PdbToNdbConverter</span> as the last step from the lists of
  names extracted.  It is handed off to the <span class="classname">StructureMap</span>, which then
  uses it throughout the rest of the application.
  <p>
  Non-protein chains present their own issues - 
  
  <p>
  From John Beaver (edited):</p>
  <blockquote>
  	<p>
  		Pdb and Ndb deal with one of the major legacy problems of the PDB data.
  		</p><p>
		The old .pdb file format
		has been around for a very long time. It's simple, and it's what most 
		people who don't use the website use. It has several technical 
		limitations, but the data matches the 
		original author submission very closely.
		</p><p>
		This is a problem. Very commonly, a small molecule or 
		DNA strand will have the same chain ID as a protein chain, for example. 
		This can cause problems when the viewer is deciding where to draw 
		ribbons and bonds.
		</p><p>
		The Ndb (whose name I took from one of the Xml tags in the PDB XML 
		format and which may or may not be proper terminology) is a separate namespace 
		for chain IDs and residue IDs. It is much more highly cleaned; you'll 
		almost never see a small molecule or DNA chain mixed with protein in one 
		chain. Also, PDB residue IDs can have letters in them; NDB residue IDs 
		are always integers.
		</p><p>
		The Ndb namespace still has data cleanliness problems, but it seems much 
		better overall than the Pdb namespace.
		</p><p>
		For an example of what I mean, look at the following .xml snippet.
		Scroll about halfway down the file, and you'll see something like...
		</p>
		<code>
		      &lt;PDBx:atom_site id="1249"&gt;
		         &lt;PDBx:group_PDB&gt;ATOM&lt;/PDBx:group_PDB&gt;
		         &lt;PDBx:type_symbol&gt;C&lt;/PDBx:type_symbol&gt;
		         &lt;PDBx:label_atom_id&gt;CG&lt;/PDBx:label_atom_id&gt;
		         &lt;PDBx:label_alt_id xsi:nil="true" /&gt;
		         &lt;PDBx:label_comp_id&gt;ARG&lt;/PDBx:label_comp_id&gt;
		         &lt;PDBx:label_asym_id&gt;A&lt;/PDBx:label_asym_id&gt;             (--&gt; NDB chain ID)
		         &lt;PDBx:label_entity_id&gt;1&lt;/PDBx:label_entity_id&gt;
		         &lt;PDBx:label_seq_id&gt;165&lt;/PDBx:label_seq_id&gt;             (--&gt; NDB residue ID)
		         &lt;PDBx:Cartn_x&gt;15.583&lt;/PDBx:Cartn_x&gt;
		         &lt;PDBx:Cartn_y&gt;0.027&lt;/PDBx:Cartn_y&gt;
		         &lt;PDBx:Cartn_z&gt;-10.746&lt;/PDBx:Cartn_z&gt;
		         &lt;PDBx:occupancy&gt;1.00&lt;/PDBx:occupancy&gt;
		         &lt;PDBx:B_iso_or_equiv&gt;26.76&lt;/PDBx:B_iso_or_equiv&gt;
		         &lt;PDBx:auth_seq_id&gt;165&lt;/PDBx:auth_seq_id&gt;               (--&gt; PDB residue ID)
		         &lt;PDBx:auth_comp_id&gt;ARG&lt;/PDBx:auth_comp_id&gt;
		         &lt;PDBx:auth_asym_id&gt;E&lt;/PDBx:auth_asym_id&gt;               (--&gt; PDB chain ID)
		         &lt;PDBx:auth_atom_id&gt;CG&lt;/PDBx:auth_atom_id&gt;
		         &lt;PDBx:pdbx_PDB_model_num&gt;1&lt;/PDBx:pdbx_PDB_model_num&gt;
		      &lt;/PDBx:atom_site&gt;
		</code>
		<p>
		Here, label_asym_id is the NDB chain ID and auth_asym_id is the PDB 
		chain ID. Similarly, label_seq_id is the NDB residue ID and auth_seq_id 
		is the PDB residue ID.
		</p><p>
		To make matters worse, Phil Bourne insisted that the community prefers 
		to see the PDB nomenclature. This is correct, since most of the 
		community uses the .pdb format. Whereas the NDB nomenclature is *much* 
		more amenable to use in the internal data structures, I had to make a 
		large dictionary to translate NDB to PDB (and vice-versa) to make sure 
		that I always displayed the PDB nomenclature in the interface.
		</p><p>
		So, in summary, NDB is the residue and chain ID nomenclature which I 
		used internally for efficiency purposes, and PDB is the nomenclature I 
		displayed to the user. The PdbToNdbConverter class handles conversion 
		between the two.
		</p><p class="newidea">
		I used 
		two sections of the PDB XML schema to construct the hashes in 
		PdbToNdbConverter. You'll have to look at the parser to be sure, but I 
		think they were entity_poly_seqCategory and pdbx_entity_nonpolyCategory. 
		These sections provide a whole new can of worms, including chain ID 
		overlaps (even for NDB IDs).
		</p><p>
		Add to that the fact that data errors in all of the formats are not 
		uncommon, which tends to further complicate the issue.
		</p>
  </blockquote>

<?php
  include_once "resources/snippets/suffix.php";
?>