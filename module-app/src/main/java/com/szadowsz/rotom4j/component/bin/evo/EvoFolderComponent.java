package com.szadowsz.rotom4j.component.bin.evo;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.rotom4j.file.data.evo.EvolutionNFSFile;
import com.szadowsz.rotom4j.file.data.evo.data.EvoMethod;
import com.szadowsz.rotom4j.ref.ItemDex;
import com.szadowsz.rotom4j.ref.PokeDex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

public class EvoFolderComponent extends RFolder {

    protected Logger LOGGER = LoggerFactory.getLogger(EvoFolderComponent.class);
    private EvolutionNFSFile evo;
    private int index;

    private final String EVO_NUM = "Evolution";

    private final String METHOD_NAME = "Method";
    private final String REQ_NAME = "Requirement";
    private final String SPECIES_NAME = "Evolve to Species";

    public EvoFolderComponent(RotomGui gui, String path, RFolder parentFolder, EvolutionNFSFile evo) {
        super(gui, path, parentFolder);
        this.evo = evo;
        lazyInitNodes();
    }

    public EvolutionNFSFile getEvoFile() {
        return evo;
    }

    protected int getIndexValue() {
        EvoSlider node = ((EvoSlider) findChildByName(EVO_NUM));
        return node.getValueAsInt();
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
        EvoTextField node = ((EvoTextField) findChildByName(nodeName));
        node.setText(valueString);
    }

    protected void setValue(String nodeName, int valueString) {
        EvoSlider node = ((EvoSlider) findChildByName(nodeName));
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
        children.add(new EvoSlider.EvoSelectSlider(gui,path + "/" + EVO_NUM, this));
        children.add(new EvoTextField(gui,path + "/" + METHOD_NAME, this,getMethodString()));
        children.add(new EvoTextField(gui,path + "/" + REQ_NAME,this, getRequirementString()));
        children.add(new EvoTextField(gui,path + "/" + SPECIES_NAME, this, getSpeciesString()));
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, name);
        drawBackdropRight(pg, RLayoutStore.getCell());
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
}
