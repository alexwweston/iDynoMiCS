function plotMix_t(nFile,sName, c1, c2)
% plotMix_t(nFile,sName, c1, c2)
%
% plots agents and the solute sName for the given iterate nFile
% (the options for sName are the same as in plotContour)
% c1 and c2 are contour boundaries

if nargin < 1
	help plotMix;
	return;
end

if nargin < 2
	fprintf('Need to specify contour to plot.\n');
	showRunInfo(nFile);
	return;
end

plotContour_t(nFile,sName, c1, c2);
plotAgents_t(nFile,sName);
