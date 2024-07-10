import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IQRCode } from '../qr-code.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../qr-code.test-samples';

import { QRCodeService } from './qr-code.service';

const requireRestSample: IQRCode = {
  ...sampleWithRequiredData,
};

describe('QRCode Service', () => {
  let service: QRCodeService;
  let httpMock: HttpTestingController;
  let expectedResult: IQRCode | IQRCode[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(QRCodeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a QRCode', () => {
      const qRCode = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(qRCode).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a QRCode', () => {
      const qRCode = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(qRCode).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a QRCode', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of QRCode', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a QRCode', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a QRCode', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addQRCodeToCollectionIfMissing', () => {
      it('should add a QRCode to an empty array', () => {
        const qRCode: IQRCode = sampleWithRequiredData;
        expectedResult = service.addQRCodeToCollectionIfMissing([], qRCode);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(qRCode);
      });

      it('should not add a QRCode to an array that contains it', () => {
        const qRCode: IQRCode = sampleWithRequiredData;
        const qRCodeCollection: IQRCode[] = [
          {
            ...qRCode,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addQRCodeToCollectionIfMissing(qRCodeCollection, qRCode);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a QRCode to an array that doesn't contain it", () => {
        const qRCode: IQRCode = sampleWithRequiredData;
        const qRCodeCollection: IQRCode[] = [sampleWithPartialData];
        expectedResult = service.addQRCodeToCollectionIfMissing(qRCodeCollection, qRCode);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(qRCode);
      });

      it('should add only unique QRCode to an array', () => {
        const qRCodeArray: IQRCode[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const qRCodeCollection: IQRCode[] = [sampleWithRequiredData];
        expectedResult = service.addQRCodeToCollectionIfMissing(qRCodeCollection, ...qRCodeArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const qRCode: IQRCode = sampleWithRequiredData;
        const qRCode2: IQRCode = sampleWithPartialData;
        expectedResult = service.addQRCodeToCollectionIfMissing([], qRCode, qRCode2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(qRCode);
        expect(expectedResult).toContain(qRCode2);
      });

      it('should accept null and undefined values', () => {
        const qRCode: IQRCode = sampleWithRequiredData;
        expectedResult = service.addQRCodeToCollectionIfMissing([], null, qRCode, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(qRCode);
      });

      it('should return initial array if no QRCode is added', () => {
        const qRCodeCollection: IQRCode[] = [sampleWithRequiredData];
        expectedResult = service.addQRCodeToCollectionIfMissing(qRCodeCollection, undefined, null);
        expect(expectedResult).toEqual(qRCodeCollection);
      });
    });

    describe('compareQRCode', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareQRCode(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareQRCode(entity1, entity2);
        const compareResult2 = service.compareQRCode(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareQRCode(entity1, entity2);
        const compareResult2 = service.compareQRCode(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareQRCode(entity1, entity2);
        const compareResult2 = service.compareQRCode(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
