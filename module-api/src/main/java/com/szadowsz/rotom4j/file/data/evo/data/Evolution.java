package com.szadowsz.rotom4j.file.data.evo.data;

public class Evolution {


    EvoMethod method;
    int requirement;
    int resultSpecies;

    public Evolution() {
        this.method = EvoMethod.values()[0];
        this.requirement = 0;
        this.resultSpecies = 0;
    }

    public Evolution(int method, int requirement, int resultSpecies) {
        this.method = EvoMethod.values()[method];
        this.requirement = requirement;
        this.resultSpecies = resultSpecies;
    }

    public EvoMethod getMethod() {
        return method;
    }


    public int getRequirement() {
        return requirement;
    }


    public int getSpecies() {
        return resultSpecies;
    }

    public void setMethod(int method) {
        this.method = EvoMethod.values()[method];
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public void setResultSpecies(int resultSpecies) {
        this.resultSpecies = resultSpecies;
    }
}