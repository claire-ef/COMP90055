filenames = [];
pneumonia = []; % 0: normal, 1: diagnosed
contrasts = [];
homogeneitys = [];
energys = [];
etps = [];

offset = [0 1; -1 1; -1 0; -1 -1];

normalFiles = dir('data/NORMAL/*.jpeg');
pneumoniaFiles = dir('data/PNEUMONIA/*.jpeg');

files = [normalFiles pneumoniaFiles];

for iImg = 1 : numel(files)
    path_filetype = split(files(iImg).name, ".");
    filenames = [filenames; path_filetype(1)];
    if iImg <= 500 % normal
        pneumonia = [pneumonia; 0]; % 0: normal, 1: diagnosed
        %imgPath = "data/NORMAL/" + path_filetype(1) + ".jpeg";
        %imgPath = "results/Naseem/NORMAL/" + path_filetype(1) + "_recovered.png";
        %imgPath = "results/Kumar/NORMAL/" + path_filetype(1) + "_steg.png";
        imgPath = "results/C4S/NORMAL/" + path_filetype(1) + "_steg.png";
    else % pneumonia
        pneumonia = [pneumonia; 1];
        %imgPath = "data/PNEUMONIA/" + path_filetype(1) + ".jpeg";
        %imgPath = "results/Naseem/PNEUMONIA/" + path_filetype(1) + "_recovered.png";
        %imgPath = "results/Kumar/PNEUMONIA/" + path_filetype(1) + "_steg.png";
        imgPath = "results/C4S/PNEUMONIA/" + path_filetype(1) + "_steg.png";
    end
    % read cover/stego image
    img = imread(imgPath);
    % get roi
    [nRow,nColumn, npage] = size(img);
    roiX = floor(nColumn/8);
    roiY = floor(nRow/8);
    roiWidth = floor(nColumn*6/8);
    roiHeight = floor(nRow*6/8);
    roi = img(roiY+1:roiY+roiHeight, roiX+1:roiX+roiWidth);
    % compute features
    stats = graycoprops(graycomatrix(roi,'Offset',offset, 'NumLevels',256,'GrayLimits',[]));
    contrasts = [contrasts; stats.Contrast];
    homogeneitys = [homogeneitys; stats.Homogeneity];
    energys = [energys; stats.Energy];
    etps = [etps; entropy(roi)];
end
T = table(filenames, pneumonia, contrasts, homogeneitys, energys, etps);
%writetable(T,'results/original_features.csv');
%writetable(T,'results/naseem_features.csv');
%writetable(T,'results/kumar_features.csv');
writetable(T,'results/c4s_features.csv');