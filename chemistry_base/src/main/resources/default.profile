# Must be one of 'IGNORE', 'IF_NECESSARY', 'BRUKER_ONLY', 'BRUKER_IF_NECESSARY' or ALWAYS'
IsotopeHandlingMs2 =BRUKER_ONLY


NoiseThresholdSettings.intensityThreshold =0.002
NoiseThresholdSettings.maximalNumberOfPeaks =60
NoiseThresholdSettings.basePeak =NOT_PRECURSOR
NoiseThresholdSettings.absoluteThreshold =0


IsolationWindow.width =
IsolationWindow.shift =

# Mass deviation setting for MS1 spectra. Mass Deviations are always written as "X ppm (Y Da)" 
# with X and Y
# are numerical values. The ppm is a relative measure (parts per million), Da is an absolute 
# measure. For each mass, the
# maximum of relative and absolute is used.
MS1MassDeviation.allowedMassDeviation =10.0 ppm
MS1MassDeviation.standardMassDeviation =10.0 ppm
MS1MassDeviation.massDifferenceDeviation =5.0 ppm

# Mass deviation setting for MS1 spectra. Mass Deviations are always written as "X ppm (Y Da)" 
# with X and Y
# are numerical values. The ppm is a relative measure (parts per million), Da is an absolute 
# measure. For each mass, the
# maximum of relative and absolute is used.
MS2MassDeviation.allowedMassDeviation =10.0 ppm
MS2MassDeviation.standardMassDeviation =10.0 ppm

MedianNoiseIntensity =0.015

NumberOfCandidates =10

NumberOfCandidatesPerIon =1

# An adduct switch is a switch of the ionization mode within a spectrum, e.g. an ion replaces an 
# sodium adduct
# with a protonation during fragmentation. Such adduct switches heavily increase the 
# complexity of the
# analysis, but for certain adducts they might happen regularly. Adduct switches are written 
# in the
# form "a -> b, a -> c, d -> c" where a, b, c, and d are adducts and a -> b denotes an allowed switch from
# a to b within the MS/MS spectrum.
PossibleAdductSwitches.adducts =

# Can be attached to a Ms2Experiment or ProcessedInput. If PrecursorIonType is unknown, 
# CSI:FingerID will use this
# object and for all different adducts.
PossibleAdducts =[M+H]+,[M]+,[M+K]+,[M+Na]+,[M+H-H2O]+,[M+Na2-H]+,[M+2K-H]+,[M+NH4]+,[M+H3O]+,[M+MeOH+H]+,[M+ACN+H]+,[M+2ACN+H]+,[M+IPA+H]+,[M+ACN+Na]+,[M+DMSO+H]+,[M-H]-,[M]-,[M+K-2H]-,[M+Cl]-,[M-H2O-H]-,[M+Na-2H]-,M+FA-H]-,[M+Br]-,[M+HAc-H]-,[M+TFA-H]-,[M+ACN-H]-

AdductSettings.enforced = ,
AdductSettings.detectable =[M+H]+,[M-H2O+H]+,[M+NH3+H]+,[M+Na]+,[M+K]+,[M-H]-,[M+Cl]-
AdductSettings.fallback =[M+H]+,[M+Na]+,[M+K]+,[M-H]-,[M+Cl]-

# Enable/Disable the hypothesen driven recalibration of MS/MS spectra
# Must be either 'ALLOWED' or FORBIDDEN'
ForbidRecalibration =ALLOWED

# This configurations hold the information how to autodetect elements based on the given 
# formula constraints.
# Note: If the compound is already assigned to a specific molecular formula, this annotation is 
# ignored.
FormulaSettings.enforced =C,H,N,O,P
FormulaSettings.detectable =S,Br,Cl,B,Se
FormulaSettings.fallback =S

# This configurations define how to deal with isotope patterns in MS1.
# When filtering is enabled, molecular formulas are excluded if their theoretical isotope 
# pattern does not match
# the theoretical one, even if their MS/MS pattern has high score.
IsotopeSettings.filter =True
# multiplier for the isotope score. Set to 0 to disable isotope scoring. Otherwise, the score 
# from isotope
# pattern analysis is multiplied with this coefficient. Set to a value larger than one if your 
# isotope
# pattern data is of much better quality than your MS/MS data.
IsotopeSettings.multiplier =1

# This configurations define a timeout for the tree computation. As the underlying problem is 
# NP-hard, it might take
# forever to compute trees for very challenging (e.g. large mass) compounds. Setting an time 
# constraint allow the program
# to continue with other instances and just skip the challenging ones.
# Note that, due to multithreading, this time constraints are not absolutely accurate.
Timeout.secondsPerInstance =0
Timeout.secondsPerTree =0
PossibleAdductSwitches = [M+Na]+:{[M+H]+}
FormulaConstraints.alphabet = CHNOP[5]S
NormalizationType = GLOBAL
FormulaConstraints.valenceFilter = -0.5
AlgorithmProfile = default
IntensityDeviation = 0.02


