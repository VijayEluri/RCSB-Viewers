package org.rcsb.lx.controllers.scene;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.rcsb.lx.controllers.app.LigandExplorer;
import org.rcsb.lx.controllers.update.LXUpdateController;
import org.rcsb.lx.controllers.update.LXUpdateEvent;
import org.rcsb.lx.glscene.jogl.LXGlGeometryViewer;
import org.rcsb.lx.glscene.jogl.LXSceneNode;
import org.rcsb.lx.model.LXModel;
import org.rcsb.lx.ui.LXDocumentFrame;
import org.rcsb.mbt.model.Residue;
import org.rcsb.mbt.model.StructureModel;
import org.rcsb.mbt.model.Structure;
import org.rcsb.mbt.model.StructureMap;
import org.rcsb.mbt.model.attributes.ChainStyle;
import org.rcsb.mbt.model.attributes.StructureStyles;
import org.rcsb.uiApp.controllers.update.UpdateEvent;
import org.rcsb.vf.controllers.scene.SceneController;


public class LXSceneController extends SceneController
{
	private static int firstReset = 0;

	private InteractionCalculator interactionsCalculator = new InteractionCalculator();
	
	private boolean newDocument = true;
	public void setNewDocument(boolean flag) { newDocument = flag; }
	
	public void processLeftPanelEvent(final Structure structure,
			final float ligwaterbondlower, final float ligwaterbondupper, final boolean ligWaterProOn, final float intLigandBondupper, final float intLigandBondlower, final boolean intLigandOn, final boolean hbondflag,
			final float hbondlower, final float hbondupper, final boolean hydroflag,
			final float hydrolower, final float hydroupper, final boolean otherflag,
			final float otherlower, final float otherupper, final boolean displayDisLabel,
			boolean saveInteractionsToFile) {
		// XXX Status.progress(0.3f, "StructureViewer adding structure...Please wait");

		PrintWriter interactionsOut = null;
		if (saveInteractionsToFile) {
			final JFileChooser chooser = new JFileChooser();
			// chooser.addChoosableFileFilter(new FileFilter() {
			// public boolean accept(File f) {
			// return true;
			// }
			//
			// public String getDescription() {
			// return "Everything";
			// }
			// });

			chooser.addChoosableFileFilter (
				new FileFilter()
				{
					@Override
					public boolean accept(final File f)
					{
						if (f.isDirectory())
							return true;
	
						final String name = f.getName();
						final String lastFour = name.length() > 4 ? name.substring(name
								.length() - 4) : null;
						if (lastFour == null) {
							return false;
						}
	
						return lastFour.equalsIgnoreCase(".txt");
					}
	
					@Override
					public String getDescription()
						{ return ".txt (tab delimited)"; }
				});

			if (chooser.showSaveDialog(LigandExplorer.sgetActiveFrame()) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file != null) {
					try {
						if (file.getName().indexOf('.') < 0) {
							file = new File(file.getAbsolutePath() + ".txt");
						}

						interactionsOut = new PrintWriter(new java.io.FileWriter(file));

						interactionsOut
								.println("Atom 1\tAtom 2\tDistance\tType");
					} catch (final IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}

		if (!saveInteractionsToFile)
		{
			LXModel model = LigandExplorer.sgetModel();
			model.getInteractionMap().clear();

			final StructureMap structureMap = structure.getStructureMap();
			final StructureStyles structureStyles = structureMap.getStructureStyles();

			structureStyles.clearSelections();
			
			//
			// Notify the viewers that structures have been removed and added.
			// We don't want to remove the model, so we just use the update
			// controller to send a 'remove all' signal and then an 'add all'
			// signal.
			//
			// This is really pretty wanky - should look this over and fix.
			//
			LXDocumentFrame activeFrame = LigandExplorer.sgetActiveFrame();
			LXUpdateController update = LigandExplorer.sgetActiveFrame().getUpdateController();
			update.blockListener(activeFrame);
			update.removeStructure(true);
			update.fireStructureAdded(structure, false, true);
			update.unblockListener(activeFrame);
			
			LXGlGeometryViewer glViewer = LigandExplorer.sgetGlGeometryViewer();
			if (newDocument)
			{
				glViewer.resetView(true, false);
				newDocument = false;
			}

			final ChainStyle cs = (ChainStyle) structureStyles.getStyle(structureMap
					.getChain(0)); // **JB assume that all chain styles are the
			// same.
			cs.resetBinding(structure);
		}

		final StructureMap structureMap = structure.getStructureMap();

		// If its just sequence data lets bail out!
		if (structureMap.getAtomCount() < 0) {
			return;
		}

		// XXX Status.progress(0.5f, "Continue loading structure... please wait...");
		final int totalResidues = structureMap.getResidueCount();
		int onePercent = (int) (totalResidues / 100.0f);
		if (onePercent <= 0) {
			onePercent = 1;
		}

		// added for protein-ligand interaction. calculate interaction with H2O
		// in the binding site
		if (ligWaterProOn) {
			interactionsCalculator.calWaterInteractions(structure, ligwaterbondlower, ligwaterbondupper, displayDisLabel, interactionsOut);
		}

		// added for protein-ligand interactions
		if (intLigandOn) {
			interactionsCalculator.calInterLigInteractions(structure, intLigandBondlower, intLigandBondupper, displayDisLabel, interactionsOut);
		}

		// added for protein-ligand interactions
		interactionsCalculator.calculateInteractions(structure, hbondflag, hydroflag, otherflag,
				hbondupper, hbondlower, hydroupper, hydrolower, otherupper,
				otherlower, displayDisLabel, interactionsOut);

		// Center the view at the ligand
		// Status.progress(0.95f, "Centering the view at the ligand" +
		// inLigName+"...Please wait");
		if (!saveInteractionsToFile) {
			LigandExplorer.sgetGlGeometryViewer().ligandView(structure);
		}

		if (interactionsOut != null) {
			interactionsOut.close();
		}
		
		LigandExplorer.sgetUpdateController().fireInteractionChanged();

		// XXX Status.progress(1.0f, null);
	}
	
	public void setLigandResidues(final Residue[] residues)
	{
		interactionsCalculator.currentLigandResidues = residues;
	}
	
	public Residue[] getLigandResidues() { return interactionsCalculator.currentLigandResidues; }
	
	public void clearStructure(boolean transitory)
	{
		if (!transitory)
			interactionsCalculator.currentLigandResidues = null;
	}
	
	/**
	 * Reset the view to look at the center of the data. JLM DEBUG: This will
	 * eventually be non-static method.
	 * 
	 * Deliberately hides base implementation
	 */
	public void resetView(final boolean forceRecalculation)
	{
		StructureModel model = LigandExplorer.sgetModel();
		if (!model.hasStructures())
			return;

		StructureModel.StructureList structures = model.getStructures();
		
		for (Structure struc : structures)
		{
			final StructureMap sm = struc.getStructureMap();
			final LXSceneNode scene = (LXSceneNode)sm.getUData();

			if (firstReset < structures.size() || forceRecalculation) {

				scene.rotationCenter = sm.getAtomCoordinateAverage();
				scene.bounds = sm.getAtomCoordinateBounds();
				scene.bigX = Math.max(Math.abs(scene.bounds[0][0]), Math
						.abs(scene.bounds[1][0]));
				scene.bigY = Math.max(Math.abs(scene.bounds[0][1]), Math
						.abs(scene.bounds[1][1]));
				scene.bigZ = Math.max(Math.abs(scene.bounds[0][2]), Math
						.abs(scene.bounds[1][2]));
				firstReset++;
			}
			final double maxDistance = Math.sqrt(scene.bigX * scene.bigX
					+ scene.bigY * scene.bigY + scene.bigZ * scene.bigZ);

			// float[] eye = { 0.0, 0.0, maxDistance * 1.4 };
			final double[] eye = { scene.rotationCenter[0],
					scene.rotationCenter[1],
					scene.rotationCenter[2] + maxDistance * 1.4f };
			final double[] up = { 0.0f, 1.0f, 0.0f };
			scene.lookAt(eye, scene.rotationCenter, up);
		}
	}

	@Override
	public void handleUpdateEvent(UpdateEvent evt)
	{
		boolean transitory = (evt instanceof LXUpdateEvent)?
			transitory = ((LXUpdateEvent)evt).transitory : false;
			
		if (evt.action == UpdateEvent.Action.STRUCTURE_REMOVED)
			clearStructure(transitory);
		
		if (evt.action == UpdateEvent.Action.STRUCTURE_ADDED)
			if (!transitory) newDocument = true;
		
		super.handleUpdateEvent(evt);
	}
}
