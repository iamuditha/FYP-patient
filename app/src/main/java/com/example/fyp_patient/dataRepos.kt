package com.example.fyp_patient

object dataRepos {

    val testNames = listOf<String>(
        "17-OHP",
        "17-OH Progesterone",
        "17-OHP",
        "17-OH Progesterone",
        "HIAA",
        "Serotonin Metabolite",
        "HIAA",
        "Serotonin Metabolite",
        "Anti-malignin antibody",
        "Anti-malignin antibody",
        "Paracetamol",
        "Paracetamol",
        "ALB",
        "ALB",
        "Alcohol Dependence",
        "Alcohol Use Disorder",
        "NH3",
        "NH3",
        "Amniocentesis",
        "Amnio",
        "Culture - amniotic fluid",
        "Culture - amniotic cells",
        "Fetal Lung Maturity Tests",
        "Amniocentesis",
        "Amnio",
        "Culture - amniotic fluid",
        "Culture - amniotic cells",
        "Fetal Lung Maturity Tests",
        "Amy",
        "Amy",
        "AD",
        "AD",
        "About Anemia",
        "Liver Kidney Microsomal Type 1 Antibodies",
        "LKM1 Antibodies",
        "Anti-Liver/Kidney Microsomal Antibodies Type 1",
        "Anti-LKM1",
        "Anti-P450 2D6 Antibody",
        "Liver Kidney Microsomal Type 1 Antibodies",
        "LKM1 Antibodies",
        "Anti-Liver/Kidney Microsomal Antibodies Type 1",
        "Anti-LKM1",
        "Anti-P450 2D6 Antibody",
        "Antibody to ds-DNA",
        "Native double-stranded DNA Antibody",
        "anti-DNA",
        "Double stranded DNA Antibody",
        "Antibody to ds-DNA",
        "Native double-stranded DNA Antibody",
        "anti-DNA",
        "Double stranded DNA Antibody",
        "Functional Antithrombin III",
        "AT III",
        "AT 3",
        "Functional Antithrombin III",
        "AT III",
        "AT 3",
        "Autoimmune antibodies",
        "Autoimmune antibodies",
        "Immunoglobulin Gene Rearrangement",
        "B-cell Gene Clonality Molecular Genetic Tests",
        "BCGR",
        "BCR/ABL",
        "bcr-abl",
        "Oncogene",
        "Philadelphia Chromosome",
        "BCR/ABL",
        "bcr-abl",
        "Oncogene",
        "Philadelphia Chromosome",
        "Total Bilirubin",
        "TBIL",
        "Neonatal Bilirubin",
        "Direct Bilirubin",
        "Conjugated Bilirubin",
        "Indirect Bilirubin",
        "Unconjugated Bilirubin",
        "Total Bilirubin",
        "TBIL",
        "Neonatal Bilirubin",
        "Direct Bilirubin",
        "Conjugated Bilirubin",
        "Indirect Bilirubin",
        "Unconjugated Bilirubin",
        "Insulin C-peptide",
        "Connecting Peptide Insulin",
        "Proinsulin C-peptide",
        "Insulin C-peptide",
        "Connecting Peptide Insulin",
        "Proinsulin C-peptide",
        "CA 125 Tumor Marker",
        "CA 125 Tumor Marker",
        "CBC",
        "Hemogram",
        "CBC with Differential (CBC with diff)",
        "CK MB",
        "CPK MB",
        "CK MB",
        "CPK MB",
        "Human Calcitonin",
        "Thyrocalcitonin",
        "Human Calcitonin",
        "Thyrocalcitonin",
        "Total Calcium",
        "Ionized Calcium",
        "Total Calcium",
        "Ionized Calcium",
        "Fecal Calprotectin",
        "Stool Calprotectin",
        "Fecal Calprotectin",
        "Stool Calprotectin",
        "Total Carbamazepine",
        "Total Carbamazepine",
        "Dopamine",
        "Epinephrine",
        "Norepinephrine",
        "Free Catecholamines, plasma and urine",
        "Fractionated Catecholamines",
        "Dopamine",
        "Epinephrine",
        "Norepinephrine",
        "Free Catecholamines, plasma and urine",
        "Fractionated Catecholamines",
        "Ceruloplasmin, serum",
        "Ceruloplasmin, serum",
        "Cl",
        "Cl",
        "Blood Cholesterol",
        "Blood Cholesterol",
        "Chymotrypsin, fecal",
        "Chymotrypsin, fecal",
        "C1",
        "C1q",
        "C2",
        "C3",
        "C4",
        "CH50",
        "CH100",
        "Total Complement",
        "C1",
        "C1q",
        "C2",
        "C3",
        "C4",
        "CH50",
        "CH100",
        "Total Complement",
        "Cu",
        "Urine Copper",
        "Blood Copper",
        "Free Copper",
        "Hepatic Copper",
        "Cu",
        "Urine Copper",
        "Blood Copper",
        "Free Copper",
        "Hepatic Copper",
        "Urinary Cortisol",
        "Salivary Cortisol",
        "Free Cortisol",
        "Dexamethasone Suppression Test",
        "DST",
        "ACTH Stimulation Test",
        "Urinary Cortisol",
        "Salivary Cortisol",
        "Free Cortisol",
        "Dexamethasone Suppression Test",
        "DST",
        "ACTH Stimulation Test",
        "Creat",
        "Blood Creatinine",
        "Serum Creatinine",
        "Urine Creatinine",
        "Creat",
        "Blood Creatinine",
        "Serum Creatinine",
        "Urine Creatinine",
        "Creatinine Clearance, Urine",
        "CRCL",
        "CCT",
        "Creatinine Clearance, Urine",
        "CRCL",
        "CCT",
        "Cryocrit",
        "Cryoprotein",
        "Cryocrit",
        "Cryoprotein",
        "Common Questions",
        "Common Questions",
        "Fragment D-dimer",
        "Fibrin Degradation Fragment",
        "Fragment D-dimer",
        "Fibrin Degradation Fragment",
        "DHEA-SO4",
        "DHEA Sulfate",
        "DHEA-SO4",
        "DHEA Sulfate",
        "Serum Digoxin Level",
        "Serum Digoxin Level",
        "Serum Protein Electrophoresis",
        "Protein ELP",
        "SPE",
        "SPEP",
        "Gel Electrophoresis",
        "Capillary Electrophoresis",
        "Immunosubtraction Electrophoresis",
        "Urine Protein Electrophoresis",
        "UPE",
        "UPEP",
        "IFE",
        "CSF Protein Electrophoresis",
        "Electrophoresis",
        "EPO",
        "EPO",
        "Estrogen Fractions/fractionated",
        "Estrone (E1)",
        "Estradiol (E2)",
        "Estriol (E3)",
        "Estrogenic Hormones",
        "Estrogen Fractions/fractionated",
        "Estrone (E1)",
        "Estradiol (E2)",
        "Estriol (E3)",
        "Estrogenic Hormones",
        "Ethyl Alcohol",
        "Alcohol",
        "EtOH",
        "Blood Alcohol Level",
        "BAL",
        "Blood Alcohol Content",
        "BAC",
        "Ethyl Alcohol",
        "Alcohol",
        "EtOH",
        "Blood Alcohol Level",
        "BAL",
        "Blood Alcohol Content",
        "BAC",
        "HES/Leukemia",
        "4q12 (CHIC2) deletion",
        "PDGFRA-FIP1L1 gene rearrangement",
        "FIP1-like-1/platelet-derived growth factor alpha",
        "HES/Leukemia",
        "4q12 (CHIC2) deletion",
        "PDGFRA-FIP1L1 gene rearrangement",
        "FIP1-like-1/platelet-derived growth factor alpha",
        "Serum Ferritin",
        "Serum Ferritin",
        "Factor I Assay",
        "Fibrinogen Activity (Functional)",
        "Fibrinogen Antigen",
        "Cardiac Fibrinogen",
        "Factor I Assay",
        "Fibrinogen Activity (Functional)",
        "Fibrinogen Antigen",
        "Cardiac Fibrinogen",
        "Glycated Serum Protein",
        "GSP",
        "Glycated Serum Protein",
        "GSP",
        "G-6-PD",
        "RBC G6PD test",
        "G-6-PD",
        "RBC G6PD test",
        "Common Questions",
        "Common Questions",
        "ERBB2",
        "HER2/neu",
        "c-erbB-2",
        "erb-b2 receptor tyrosine kinase 2",
        "Human epidermal growth factor receptor 2",
        "ERBB2",
        "HER2/neu",
        "c-erbB-2",
        "erb-b2 receptor tyrosine kinase 2",
        "Human epidermal growth factor receptor 2",
        "HLA-B27 Antigen",
        "HLA-B27 Antigen",
        "HPT",
        "Hemoglobin-binding Protein",
        "Hp",
        "HPT",
        "Hemoglobin-binding Protein",
        "Hp",
        "Hct",
        "Crit",
        "Packed Cell Volume",
        "PCV",
        "H and H (Hemoglobin and Hematocrit)",
        "Hct",
        "Crit",
        "Packed Cell Volume",
        "PCV",
        "H and H (Hemoglobin and Hematocrit)",
        "Hgb",
        "Hb",
        "H and H (Hemoglobin and Hematocrit)",
        "Hgb",
        "Hb",
        "H and H (Hemoglobin and Hematocrit)",
        "Common Questions",
        "Common Questions",
        "Plasma Total Homocysteine",
        "Urine Homocysteine",
        "Homocysteine Cardiac Risk",
        "Plasma Total Homocysteine",
        "Urine Homocysteine",
        "Homocysteine Cardiac Risk",
        "Fasting Insulin",
        "Fasting Insulin",
        "IL-6",
        "IL-6",
        "Serum Iron",
        "Serum Fe",
        "Serum Iron",
        "Serum Fe",
        "Lactic Acid",
        "CSF Lactate",
        "Lactic Acid",
        "CSF Lactate",
        "Fecal Lactoferrin",
        "Stool Lactoferrin",
        "Fecal WBC Non-microscopic",
        "Fecal Lactoferrin",
        "Stool Lactoferrin",
        "Fecal WBC Non-microscopic",
        "Blood Lead Test",
        "Blood Lead Level",
        "BLL",
        "Blood Lead Test",
        "Blood Lead Level",
        "BLL",
        "Common Questions",
        "Common Questions",
        "Common Questions",
        "Common Questions",
        "LPS",
        "LPS",
        "Coronary Risk Panel",
        "Lipid Profile",
        "Fasting Lipid Panel",
        "Non-fasting Lipid Panel",
        "Cholesterol Panel",
        "Lipid Test",
        "Common Questions",
        "Common Questions",
        "Hepatic Disease",
        "Platelet-activating Factor Acetylhydrolase",
        "PAF-AH",
        "PLAC",
        "Platelet-activating Factor Acetylhydrolase",
        "PAF-AH",
        "PLAC",
        "LA",
        "LAC",
        "Lupus Inhibitor",
        "LA Sensitive PTT",
        "PTT-LA",
        "Dilute Russell Viper Venom Test",
        "DRVVT",
        "Modified Russell Viper Venom Test",
        "MRVVT",
        "Mg",
        "Mag",
        "Mg",
        "Mag",
        "Hg (chemical symbol)",
        "Hg (chemical symbol)",
        "MTX",
        "Amethopterin",
        "MTX",
        "Amethopterin",
        "Mycoplasma by PCR",
        "Mycoplasma Culture",
        "Ureaplasma Culture",
        "Mycoplasma by PCR",
        "Mycoplasma Culture",
        "Ureaplasma Culture",
        "Urine Myoglobin",
        "Serum Myoglobin",
        "Urine Myoglobin",
        "Serum Myoglobin",
        "Differential Display Code 3",
        "DD3",
        "Differential Display Code 3",
        "DD3",
        "Programmed Cell Death -Ligand 1 (PD-L1)",
        "Immune Checkpoint Inhibitor",
        "Programmed Cell Death -Ligand 1 (PD-L1)",
        "Immune Checkpoint Inhibitor",
        "PML-RARA t(15;17)(q22;q12)",
        "Acute Promyelocytic Leukemia",
        "AML-M3",
        "PML-RARA t(15;17)(q22;q12)",
        "Acute Promyelocytic Leukemia",
        "AML-M3",
        "PS1",
        "Presenilin 1 Gene",
        "S182",
        "PS1",
        "Presenilin 1 Gene",
        "S182",
        "Pb",
        "Phenobarb",
        "Pb",
        "Phenobarb",
        "Phenytoin, total and free",
        "Phenytoin, total and free",
        "P",
        "PO",
        "4",
        "Phosphate",
        "P",
        "PO",
        "4",
        "Phosphate",
        "K",
        "K",
        "Thyroxine-binding Prealbumin",
        "Thyroxine-binding Prealbumin",
        "Common Questions",
        "Common Questions",
        "PCT",
        "PCT",
        "PGSN",
        "PGSN",
        "PRL",
        "PRL",
        "Retic Count",
        "Reticulocyte Percent",
        "Reticulocyte Index",
        "Corrected Reticulocyte",
        "Reticulocyte Production Index",
        "RPI",
        "Retic Count",
        "Reticulocyte Percent",
        "Reticulocyte Index",
        "Corrected Reticulocyte",
        "Reticulocyte Production Index",
        "RPI",
        "5-Hydroxytryptamine",
        "5-HT",
        "5-Hydroxytryptamine",
        "5-HT",
        "Rapamycin",
        "Rapamycin",
        "Na",
        "Na",
        "T-cell Gene Clonality",
        "TCR Gene Rearrangement",
        "TCGR",
        "TORCH Panel",
        "TORCH Panel",
        "FK506",
        "TAC",
        "FK506",
        "TAC",
        "Total Testosterone",
        "Free Testosterone",
        "Bioavailable Testosterone",
        "Total Testosterone",
        "Free Testosterone",
        "Bioavailable Testosterone",
        "Tg",
        "TGB",
        "Tg",
        "TGB",
        "TG",
        "TRIG",
        "TG",
        "TRIG",
        "TnI",
        "TnT",
        "cTnI",
        "cTnT",
        "high-sensitivity troponin",
        "hs-troponin",
        "TnI",
        "TnT",
        "cTnI",
        "cTnT",
        "high-sensitivity troponin",
        "hs-troponin",
        "Mast Cell Tryptase",
        "Alpha Tryptase",
        "Beta Tryptase",
        "Mature Tryptase",
        "Mast Cell Tryptase",
        "Alpha Tryptase",
        "Beta Tryptase",
        "Mature Tryptase",
        "Urine Test",
        "Urine Analysis",
        "UA",
        "Urine Test",
        "Urine Analysis",
        "UA",
        "Common Questions",
        "Common Questions"
    )
    fun testNamesList(): List<String> {
        return testNames
    }
}