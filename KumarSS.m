%clear;
%clc;

% set Generator polynomial for different PN Sequence length
% according to https://au.mathworks.com/help/comm/ref/pnsequencegenerator.html
polynomials = dictionary(22:30, {[22 21 0], [23 18 0], [24 23 22 17 0], [25 22 0], [26 25 24 20 0], ...
    [27 26 25 22 0], [28 25 0], [29 27 0], [30 29 28 7 0]});

% ======= experiment with different watermark length and embedding strength
% bbp = [0.0002, 0.0004, 0.0006, 0.0008, 0.001, 0.0015, 0.002];
% gainFactor = [0.1, 0.2, 0.3, 0.5, 0.8, 1];

% get data files
% files = dir('data/PNEUMONIA/*.jpeg');

% record PSNR, SSIM and BER
% PSNR = zeros(numel(bbp), numel(gainFactor), numel(files));
% SSIM = zeros(numel(bbp), numel(gainFactor), numel(files));
% BER = zeros(numel(bbp), numel(gainFactor), numel(files));
% ======= chosen: bbp = 0.0002, k = 0.2

bbp = 0.0002;
k = 0.2;

% read the whole watermark to embed
wholewatermark = readmatrix('watermark.txt');

% perform extraction on attacked watermarked images
attacks = ["gaussianNoise", "speckleNoise", "saltAndPepperNoise", "contrastAdjustment"];
for iAttack = 1:numel(attacks)
    attack = attacks(iAttack);
    files = dir('attackedImages/' + attack+'/Kumar/*.png');
    for iImg = 1 : numel(files)
        path_filetype = split(files(iImg).name, ".");
        imgPath = "attackedImages/" + attack + "/Kumar/" + files(iImg).name;
        stego = imread(imgPath);
        [nRow, nColumn,] = size(stego);
        watermarkLen = ceil(nRow * nColumn * bbp);
        extractedWatermark = KumarExtract(stego, watermarkLen, polynomials);
        writematrix(extractedWatermark,"results/attackedResults/" + attack + "/Kumar/" + path_filetype(1)+'_extracted.txt');
    end
end

%for iImg = 1 : numel(pneumoniaFiles)
    %imgPath = strcat(pneumoniaFiles(iImg).folder,'/', pneumoniaFiles(iImg).name);
    % read cover image
    %cover = imread(imgPath);
    %[nRow, nColumn,] = size(cover); 
    % compute allowable watermark length
    %watermarkLen = ceil(nRow * nColumn * bbp);
    %watermark = wholewatermark(1:watermarkLen);
    % embed
    %stego = KumarEmbed(cover, watermark, polynomials, k);
    % extract
    %extractedWatermark = KumarExtract(stego, watermarkLen, polynomials);

    % save result files
    %path_filetype = split(pneumoniaFiles(iImg).name,".");
    %writematrix(extractedWatermark,"results/Kumar/PNEUMONIA/" + path_filetype(1) + '_extracted.txt');
    %imwrite(stego, "results/Kumar/PNEUMONIA/" + path_filetype(1) + "_steg.png", "PNG");
%end

%for iImg = 1 : numel(normalFiles)
    %imgPath = strcat(normalFiles(iImg).folder,'/', normalFiles(iImg).name);
    % read cover image
    %cover = imread(imgPath);
    %[nRow, nColumn,] = size(cover);
    % compute allowable watermark length
    %watermarkLen = ceil(nRow * nColumn * bbp);
    %watermark = wholewatermark(1:watermarkLen);
    % embed
    %stego = KumarEmbed(cover, watermark, polynomials, k);
    % extract
    %extractedWatermark = KumarExtract(stego, watermarkLen, polynomials);

    % save result files
    %path_filetype = split(normalFiles(iImg).name,".");
    %writematrix(extractedWatermark,"results/Kumar/NORMAL/" + path_filetype(1) + '_extracted.txt');
    %imwrite(stego, "results/Kumar/NORMAL/" + path_filetype(1) + "_steg.png", "PNG");
%end

function stegImg = KumarEmbed(cover, watermark, polynomials, k)
    [nRow, nColumn,] = size(cover);

    % Perform a level 2 wavelet decomposition of the image using the haar wavelet
    [c, s] = wavedec2(cover,2,"haar");
    % Extract the level 2 approximation and detail coefficients.
    [ccH, ccV,] = detcoef2("all", c, s, 2);

    % generate PN sequence pairs
    ndegree = ceil(log2(numel(watermark)*nRow*nColumn/16*2));
    pnSequence = comm.PNSequence("Polynomial", cell2mat(polynomials(ndegree)), "InitialConditions", [zeros(1,ndegree-1),1],"SamplesPerFrame", s(2,1)*s(2,2));
    for i = 1: numel(watermark)
        bitToEmbed = watermark(i);
        pnH = reshape(pnSequence(), [s(2,1), s(2,2)]);
        pnV = reshape(pnSequence(), [s(2,1), s(2,2)]);
        % add PN sequences to ccH and ccV components when message = 0
        if bitToEmbed == 0
            ccH = ccH + k * pnH;
            ccV = ccV + k * pnV;
        end
    end

    % update ccH and ccV in c
    ccASize = s(1,1) * s(1, 2); % size of approximation coefficients
    ccHV_nRow = s(2,1); % height of ccH/ccV
    ccHV_nCol = s(2,2); % width of ccH/ccV
   

    for i = 1 : ccHV_nCol
        for j = 1 : ccHV_nRow
            c(ccASize + (i-1)*ccHV_nRow + j) = ccH(j, i); % update ccH
            c(ccASize + ccHV_nRow * ccHV_nCol + (i-1)*ccHV_nRow + j) = ccV(j, i); % update ccV
        end
    end

    % Apply inverse “Haar” Wavelet transform to get the final stego image
    stegImg = uint8(waverec2(c,s,"haar"));
end

function watermark = KumarExtract(stego, watermarkLen, polynomials)
    [nRow, nColumn,] = size(stego);

    % Perform a level 2 wavelet decomposition of the image using the haar wavelet
    [c, s] = wavedec2(stego,2,"haar");
    % Extract the level 2 approximation and detail coefficients.
    [ccH, ccV,] = detcoef2("all", c, s, 2);
    % generate one's sequences
    watermark = ones(1, watermarkLen);

    corrH = zeros(1, watermarkLen);
    corrV = zeros(1, watermarkLen);

    % generate PN sequence pairs
    ndegree = ceil(log2(numel(watermark)*nRow*nColumn/16*2));
    pnSequence = comm.PNSequence("Polynomial", cell2mat(polynomials(ndegree)), "InitialConditions", [zeros(1,ndegree-1),1], "SamplesPerFrame", s(2,1)*s(2,2));
    for i = 1: numel(watermark)
        pnH = reshape(pnSequence(), [s(2,1), s(2,2)]);
        pnV = reshape(pnSequence(), [s(2,1), s(2,2)]);
        
        % calculate correlations
        corrcoefH = corrcoef(ccH, pnH);
        corrH(i) = corrcoefH(1,2);
        corrcoefV = corrcoef(ccV, pnV);
        corrV(i) = corrcoefV(1,2);
    end

    % calculate average correlation
    avg_corr = (corrH + corrV)/2;

    % calculate mean correlation
    mean_corr = mean(avg_corr);
    % disp(mean_corr);

    % extract 0 bit
    for i = 1: numel(watermark)
        if avg_corr(i) > mean_corr
            watermark(i) = 0;
        end
    end
end


