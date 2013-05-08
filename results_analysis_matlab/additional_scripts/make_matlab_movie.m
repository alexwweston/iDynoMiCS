function make_matlab_movie(nFrames,sName, c1, c2, fileName)
% make_matlab_movie(nFrames,sName, c1, c2, fileName)
% calls plotMix_t for each iterate up to nFrames,
% generates .avi movie based on these plots
if nargin < 1
	help plotMix;
	return;
end

if nargin < 2 
	fprintf('Need to specify contour to plot.\n');
	showRunInfo(nFile);
	return;
end

if nargin < 4
	fprintf('Need to specify .avi output filename\n');
	return;
end

% Preallocate movie structure.
mov(1:nFrames) = struct('cdata', [],...
                        'colormap', []);

% Create movie.

for i=0:(nFrames/2)
   plotMix_t(i*2,sName,c1, c2)
   mov(i+1) = getframe(gcf);
end

% Create AVI file.
%uncomment next line to play movie in matlab
%movie(mov, 1, 1)
close all
movie2avi(mov, fileName,'fps',1, 'compression', 'None');
