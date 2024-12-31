package com.szadowsz.nds4j.app.nodes.bin.evo;

import com.google.gson.JsonElement;

import com.szadowsz.nds4j.file.bin.evo.data.EvoMethod;
import com.szadowsz.nds4j.ref.ItemDex;
import com.szadowsz.nds4j.ref.PokeDex;
import com.szadowsz.nds4j.file.bin.evo.EvolutionNFSFile;
import com.old.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import static com.old.ui.store.LayoutStore.cell;


public class EvoFolderNode extends FolderNode {

    protected Logger LOGGER = LoggerFactory.getLogger(EvoFolderNode.class);
    private EvolutionNFSFile evo;
    private int index;

    private final String EVO_NUM = "Evolution";

    private final String METHOD_NAME = "Method";
    private final String REQ_NAME = "Requirement";
    private final String SPECIES_NAME = "Evolve to Species";

    public EvoFolderNode(String path, FolderNode parentFolder, EvolutionNFSFile evo) {
        super(path, parentFolder);
        this.evo = evo;
        lazyInitNodes();
        idealWindowWidthInCells = 7;
    }

    public EvolutionNFSFile getEvoFile() {
        return evo;
    }

    protected int getIndexValue() {
        EvoSliderNode node = ((EvoSliderNode) findChildByName(EVO_NUM));
        return (int) node.valueFloat;
    }

    public int getNumEvolutions(){
        return evo.getNumEvolutions();
    }

    public EvoMethod getMethod() {
        return evo.getMethod(index);
    }

    public int getRequirement() {
        return evo.getRequirement(index);
    }

    public int getSpecies() {
        return evo.getSpecies(index);
    }

    public String getMethodString() {
        return getMethod().toString();
    }

    public String getRequirementString() {
        switch (getMethod()){
            case EVO_NONE:
                return "NONE";
            case EVO_ITEM:
            case EVO_TRADE_ITEM:
                return ItemDex.getItemNameByNo(getRequirement());
            default:
                return getRequirement() + " lvl.";
        }
    }

    public String getSpeciesString() {
        return PokeDex.getPokemonNameByNo(getSpecies());
    }

    protected void setValue(String nodeName, String valueString) {
        EvoTextNode node = ((EvoTextNode) findChildByName(nodeName));
        node.setStringValue(valueString);
    }

    protected void setValue(String nodeName, int valueString) {
        EvoSliderNode node = ((EvoSliderNode) findChildByName(nodeName));
        node.setValueFromParent(valueString);
    }

    public void setEvoFile(EvolutionNFSFile evo) {
        this.evo = evo;
        index = 0;
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new EvoSliderNode.EvoSelectNode(path + "/" + EVO_NUM, this));
        children.add(new EvoTextNode(path + "/" + METHOD_NAME, this,getMethodString()));
        children.add(new EvoTextNode(path + "/" + REQ_NAME,this, getRequirementString()));
        children.add(new EvoTextNode(path + "/" + SPECIES_NAME, this, getSpeciesString()));
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
        //       drawRightTextToNotOverflowLeftText(pg, getValueAsString(), name, true); //we need to calculate how much space is left for value after the name is displayed
    }

    void loadValues() {
       int nIndex = getIndexValue();
        if (index != nIndex){
            index = nIndex;
            if (index < 0){
                index = 0;
            } else if (index >= evo.getNumEvolutions()){
                index = evo.getNumEvolutions()-1;
            }
            setValue(EVO_NUM, index);
            setValue(METHOD_NAME, getMethodString());
            setValue(REQ_NAME, getRequirementString());
            setValue(SPECIES_NAME, getSpeciesString());
        } else{
            // NOOP
        }
    }

    @Override
    public void overwriteState(JsonElement loadedNode) {
    }
}
